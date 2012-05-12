package aeminium.gpu.operations;

import org.bridj.Pointer;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.generator.MapReduceCodeGen;
import aeminium.gpu.operations.utils.ExtractTypes;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class MapReduce<I,O> extends GenericProgram implements Program {
	
	static final int DEFAULT_MAX_REDUCTION_SIZE = 4;

	
	protected PList<I> input;
	private O output;
	protected LambdaMapper<I,O> mapFun;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	protected CLBuffer<?> inbuffer;
	private CLBuffer<O> outbuffer;
	
	private MapReduceCodeGen gen;
	
	private int blocks;
	private int threads;
	private int current_size;
	
	// Constructors
	
	public MapReduce(LambdaMapper<I,O> mapper, LambdaReducerWithSeed<O> reducer, PList<I> list, String other, GPUDevice dev) {
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
	
	private String mergeComplexities(String one, String two) {
		if (one == null || one.length() == 0) return two;
		if (two == null || two.length() == 0) return one;
		return one + "+" + two;
	}
	
	protected boolean willRunOnGPU() {
		return OpenCLDecider.useGPU(input.size(), 1, mapFun.getSource() + reduceFun.getSource(),  mergeComplexities(mapFun.getSourceComplexity(), reduceFun.getSourceComplexity()));
	}
	
	
	public void cpuExecution() {
		O accumulator = this.getReduceFun().getSeed();
		for (int i = 0; i < input.size(); i++) {
			accumulator = reduceFun.combine( mapFun.map(input.get(i)), accumulator);
		}
		output = accumulator;
	}
	
	// Pipeline
	
	@Override
	public String getSource() {
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
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		CLBuffer<?>[] tempBuffers = new CLBuffer<?>[2];
		int depth = 0;
		CLEvent[] eventsArr = new CLEvent[1];
		int[] blockCountArr = new int[1];
		current_size = input.length();

		while (current_size > 1) {
			int blocksInCurrentDepth = current_size
					/ DEFAULT_MAX_REDUCTION_SIZE;
			if (current_size > blocksInCurrentDepth
					* DEFAULT_MAX_REDUCTION_SIZE) {
				blocksInCurrentDepth++;
			}
			int iOutput = depth & 1;
			CLBuffer<?> currentInput = (depth == 0) ? inbuffer : tempBuffers[iOutput ^ 1];
			outbuffer = (CLBuffer<O>) tempBuffers[iOutput];
			if (outbuffer == null) {
				tempBuffers[iOutput] = BufferHelper.createInputOutputBufferFor(ctx, getOutputType(), blocksInCurrentDepth);
				outbuffer = (CLBuffer<O>) tempBuffers[iOutput];
			}
			synchronized (kernel) {
				kernel.setArgs(inbuffer, currentInput, outbuffer, (long) current_size,
						(long) blocksInCurrentDepth,
						(long) DEFAULT_MAX_REDUCTION_SIZE, (depth == 0) ? 1 : 0);
				int workgroupSize = blocksInCurrentDepth;
				if (workgroupSize == 1)
					workgroupSize = 2;
				blockCountArr[0] = workgroupSize;
				eventsArr[0] = kernel.enqueueNDRange(q, blockCountArr, null,
						eventsArr);
			}
			current_size = blocksInCurrentDepth;
			depth++;
		}

		kernelCompletion = eventsArr[eventsArr.length - 1];
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
		execute();
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

	public LambdaReducerWithSeed<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaReducerWithSeed<O> reduceFun) {
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
