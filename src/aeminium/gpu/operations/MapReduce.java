package aeminium.gpu.operations;

import org.bridj.Pointer;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.lists.PList;
import aeminium.gpu.lists.lazyness.Range;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.generator.MapReduceCodeGen;
import aeminium.gpu.operations.utils.ExtractTypes;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel.LocalSize;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class MapReduce<I,O> extends GenericProgram implements Program {
	
	protected PList<I> input;
	private O output;
	protected LambdaMapper<I,O> mapFun;
	protected LambdaReducer<O> reduceFun;
	
	protected CLBuffer<?> inbuffer;
	private LocalSize sharedbuffer;
	private CLBuffer<?> middlebuffer;
	private CLBuffer<?> outbuffer;
	
	private MapReduceCodeGen gen;
	
	private int blocks;
	private int threads;
	
	// Constructors
	
	public MapReduce(LambdaMapper<I,O> mapper, LambdaReducer<O> reducer, PList<I> list, String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.mapFun = mapper;
		this.reduceFun = reducer;
		this.setOtherSources(other);
		gen = new MapReduceCodeGen(this);
		if (list instanceof Range) {
			gen.setRange(true);
		}
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
		if (input instanceof Range) {
			// Fake 1 byte data.
			Pointer<Integer> ptr = Pointer.allocateInts(1).order(ctx.getByteOrder());
			inbuffer = ctx.createBuffer(CLMem.Usage.Input, ptr, false);
		} else {
			inbuffer = BufferHelper.createInputBufferFor(ctx, input);
		}
		middlebuffer = BufferHelper.createOutputBufferFor(ctx, getOutputType(), input.size());
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
			    kernel.setArgs(inbuffer, middlebuffer, outbuffer, sharedbuffer, current_size, (first) ? 1 : 0);
			    
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
				tmp = middlebuffer;
				middlebuffer = outbuffer;
				outbuffer = tmp;
			}
		}
		
		// final swap.
		outbuffer = middlebuffer;
	}
	
	public O cpuExecution() {
		O accumulator = this.getReduceFun().getSeed();
		for (int i = 0; i < input.size(); i++) {
			accumulator = reduceFun.combine( mapFun.map(input.get(i)), accumulator);
		}
		return accumulator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		//TODO: remove prints
		output = (O) BufferHelper.extractElementFromBuffer(outbuffer, q, kernelCompletion, getOutputType());
	}

	
	@Override
	public void release() {
		this.inbuffer.release();
		//this.middlebuffer.release();
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
	
	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}

	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}

	@Override
	public String getKernelName() {
		return gen.getReduceKernelName();
	}

}
