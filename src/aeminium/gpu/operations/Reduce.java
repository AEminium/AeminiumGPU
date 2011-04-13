package aeminium.gpu.operations;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.generator.ReduceCodeGen;
import aeminium.gpu.operations.utils.ExtractTypes;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel.LocalSize;
import com.nativelibs4java.opencl.CLQueue;

public class Reduce<O> extends GenericProgram implements Program {
	
	protected PList<O> input;
	private O output;
	protected LambdaReducer<O> reduceFun;
	
	protected CLBuffer<?> inbuffer;
	private LocalSize sharedbuffer;
	private CLBuffer<?> outbuffer;
	
	private ReduceCodeGen gen;
	
	private int blocks;
	private int threads;
	
	// Constructors
	
	public Reduce(LambdaReducer<O> reduceFun2, PList<O> list, GPUDevice dev) {
		this(reduceFun2, list, "", dev);
	}
	
	public Reduce(LambdaReducer<O> reduceFun, PList<O> list, String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.reduceFun = reduceFun;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
	}
	
	// only for subclasses
	protected Reduce(LambdaReducer<O> reduceFun2, GPUDevice dev) {
		this(reduceFun2, "", dev);
	}
	protected Reduce(LambdaReducer<O> reduceFun2, String other, GPUDevice dev) {
		this.device = dev;
		this.reduceFun = reduceFun2;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
	}
	
	// Pipeline
	
	@Override
	protected String getSource() {
		return gen.getReduceKernelSource();
	}
	
	public String getReduceOpenCLSource() {
		return gen.getReduceLambdaSource();
	}
	
	@Override
	public void prepareBuffers(CLContext ctx) {
		inferBestValues();
		inbuffer = BufferHelper.createInputOutputBufferFor(ctx, input);
		outbuffer = BufferHelper.createOutputBufferFor(ctx, getOutputType(), input.size());
		sharedbuffer = BufferHelper.createSharedBufferFor(ctx , getOutputType(), threads);
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		CLEvent[] previous = new CLEvent[1];
		CLBuffer<?> tmp;
		boolean first = true;
		int current_size = input.size();
		
		while(current_size > 1) {
			synchronized (kernel) {
				inferBestValues();
			    kernel.setArgs(inbuffer, outbuffer, sharedbuffer, current_size);
			    
			    int global_workgroup_size = Math.min(blocks, current_size/(threads*2)) * threads;
			    int local_workgroup_size = threads;
			    
			    kernelCompletion = kernel.enqueueNDRange(q, 
			    		new int[] { global_workgroup_size }, 
			    		new int[] { local_workgroup_size }, 
			    		(first) ? (new CLEvent[] {}) : previous);
			    
			    previous[0] = kernelCompletion;
			    first = false;
			    
			    current_size = current_size / (threads * 2);
			    
				// Swap input and output
				tmp = inbuffer;
				inbuffer = outbuffer;
				outbuffer = tmp;
			}
		}
		
		// final swap.
		outbuffer = inbuffer;
	}
	
	public O cpuExecution() {
		O accumulator = this.getReduceFun().getSeed();
		for (int i = 0; i < input.size(); i++) {
			accumulator = reduceFun.combine( input.get(i), accumulator);
		}
		return accumulator;
	}

	@SuppressWarnings("unchecked")
	public void debugBuffers(CLContext ctx, CLQueue q) {
		PList<O> li;
		
		System.out.println("i:");
		li = (PList<O>) BufferHelper.extractFromBuffer(inbuffer, q, kernelCompletion, getOutputType(), input.size());
		for (int i = 0; i < li.size(); i++) {
			System.out.print(li.get(i) + ",");
		}
		System.out.println("___");
		
		System.out.println("o:");
		li = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q, kernelCompletion, getOutputType(), input.size());
		for (int i = 0; i < li.size(); i++) {
			System.out.print(li.get(i) + ",");
		}
		System.out.println("___");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		output = (O) BufferHelper.extractElementFromBuffer(outbuffer, q, kernelCompletion, getOutputType());
	}

	
	@Override
	public void release() {
		this.inbuffer.release();
		//this.outbuffer.release();
		super.release();
	}
	
	// Helper Methods

	private void inferBestValues() {
		int max_threads = (int) device.getDevice().getMaxWorkGroupSize();
		int max_blocks = 512;
		int n = input.size();
		
		//threads = Math.min(nextPow2(n/2), max_threads);
		//blocks = Math.min(nextPow2(n/2)/threads, max_blocks);
		
		threads = (n < max_threads*2) ? nextPow2((n + 1)/ 2) : max_threads;
        blocks = (n + (threads * 2 - 1)) / (threads * 2);
        blocks = (blocks > max_blocks) ? max_blocks : blocks;
	}
	
	private int nextPow2(int i) {
        int shifted = 0;
        boolean lost = false;
        for (;;) {
            int next = i >> 1;
            if (next == 0) {
                if (lost)
                    return 1 << (shifted + 1);
                else
                    return 1 << shifted;
            }
            lost = lost || (next << 1 != i);
            shifted++;
            i = next;
        }
	}
	
	// Output
	
	public O getOutput() {
		// No need for lazyness in reduces.
		run();
		return output;
	}
	
	
	// Utils
	
	public String getOpenCLSeed() {
		return reduceFun.getSeedSource();
	}

	
	public String getInputType() {
		return input.getType().getSimpleName().toString();
	}
	
	public String getOutputType() {
		return ExtractTypes.extractReturnTypeOutOf(reduceFun, "combine");
	}
	
	public int getOutputSize() {
		return input.size();
	}
	

	// Getters and Setters
	
	public void setOutput(PList<O> output) {
		this.output = output.get(0);
	}
	
	public void setOutput(O output) {
		this.output = output;
	}

	public LambdaReducer<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaReducer<O> reduceFun) {
		this.reduceFun = reduceFun;
	}
	
	@Override
	public String getKernelName() {
		return gen.getReduceKernelName();
	}

}
