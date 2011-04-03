package aeminium.gpu.operations;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.generator.ReduceCodeGen;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
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
		inbuffer = BufferHelper.createInputBufferFor(ctx, input);
		outbuffer = BufferHelper.createOutputBufferFor(ctx, getOutputType(), input.size());
		sharedbuffer = new CLKernel.LocalSize(threads);
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		CLEvent[] previous;
		CLBuffer<?> tmp;
		//CLBuffer<?> out_original = outbuffer;
		boolean first = true;
		int current_size = input.size();
		while(current_size > 1) {
			synchronized (kernel) {
				inferBestValues();
			    kernel.setArgs(inbuffer, outbuffer, sharedbuffer, current_size);
			    
			    int global_workgroup_size = blocks * threads;
			    int local_workgroup_size = threads;
			    
			    if (first) {
			    	previous = new CLEvent[] {};
			    	first = false;
			    } else {
			    	previous = new CLEvent[] { kernelCompletion };
			    }
			    
			    kernelCompletion = kernel.enqueueNDRange(q, 
			    		new int[] { global_workgroup_size }, 
			    		new int[] { local_workgroup_size }, 
			    		previous);
			    
			    current_size = current_size / (threads * 2);
			    if (current_size > 1) {
				    // Swap input and output
				    tmp = inbuffer;
				    inbuffer = outbuffer;
				    outbuffer= tmp;
			    }
			}
		}
	}
	
	public O cpuExecution() {
		O accumulator = this.getReduceFun().getSeed();
		for (int i = 0; i < input.size(); i++) {
			accumulator = reduceFun.combine( input.get(i), accumulator);
		}
		return accumulator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		// TODO: Improve extracting one element.
		PList<O> resultList = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q, kernelCompletion, getOutputType(), 1);
		output = (O) BufferHelper.decode(resultList.get(0), getOutputType());
	}

	
	@Override
	public void release() {
		this.inbuffer.release();
		this.outbuffer.release();
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
	
	private int nextPow2(int x) {
		--x;
	    x |= x >> 1;
	    x |= x >> 2;
	    x |= x >> 4;
	    x |= x >> 8;
	    x |= x >> 16;
	    return ++x;
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
		return getInputType();
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
