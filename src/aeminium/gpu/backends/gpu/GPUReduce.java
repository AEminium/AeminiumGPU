package aeminium.gpu.backends.gpu;

import org.bridj.Pointer;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.AbstractReduceCodeGen;
import aeminium.gpu.backends.gpu.generators.MapReduceCodeGen;
import aeminium.gpu.backends.gpu.generators.ReduceCodeGen;
import aeminium.gpu.backends.gpu.generators.ReduceTemplateSource;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.utils.ExtractTypes;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class GPUReduce<I, O> extends GPUGenericKernel implements ReduceTemplateSource<O> {
	
	static final int DEFAULT_MAX_REDUCTION_SIZE = 4;
	
	protected PList<I> input;
	protected O output;
	protected LambdaMapper<I, O> mapFun;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	protected CLBuffer<?> inbuffer;
	protected CLBuffer<?> outbuffer;

	private AbstractReduceCodeGen gen;

	private int blocks;
	private int threads;
	private int current_size;
	
	
	public GPUReduce(PList<I> input, LambdaReducerWithSeed<O> reduceFun) {
		this.input = input;
		this.mapFun = null;
		this.reduceFun = reduceFun;
		
		gen = new ReduceCodeGen(this);
		if (reduceFun instanceof LambdaReducerWithSeed) {
			gen.setHasSeed(true);
		}
		otherData = OtherData.extractOtherData(reduceFun);
		gen.setOtherData(otherData);
	}
	
	public GPUReduce(PList<I> input,  LambdaMapper<I, O> mapFun, LambdaReducerWithSeed<O> reduceFun) {
		this.input = input;
		this.mapFun = mapFun;
		this.reduceFun = reduceFun;
		
		gen = new MapReduceCodeGen(this);
		if (reduceFun instanceof LambdaReducerWithSeed) {
			gen.setHasSeed(true);
		}
		if (input instanceof Range) {
			gen.setRange(true);
		}
		otherData = OtherData.extractOtherData(mapFun, reduceFun);
		gen.setOtherData(otherData);
	}

	@Override
	public String getKernelName() {
		return gen.getReduceKernelName();
	}
	
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
		if (gen instanceof MapReduceCodeGen) {
			if (input instanceof Range) {
				// Fake 1 byte data.
				Pointer<Integer> ptr = Pointer.allocateInts(1).order(
						ctx.getByteOrder());
				inbuffer = ctx.createBuffer(CLMem.Usage.Input, ptr, false);
			} else {
				inbuffer = BufferHelper.createInputBufferFor(ctx, input, end);
			}	
		} else {
			inbuffer = BufferHelper.createInputOutputBufferFor(ctx, input, end);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {

		CLBuffer<?>[] tempBuffers = new CLBuffer<?>[2];
		int depth = 0;
		CLEvent[] eventsArr = new CLEvent[1];
		int[] blockCountArr = new int[1];
		current_size = end;

		while (current_size > 1) {
			int blocksInCurrentDepth = current_size
					/ DEFAULT_MAX_REDUCTION_SIZE;
			if (current_size > blocksInCurrentDepth
					* DEFAULT_MAX_REDUCTION_SIZE) {
				blocksInCurrentDepth++;
			}
			int iOutput = depth & 1;
			CLBuffer<?> currentInput = (depth == 0) ? inbuffer
					: tempBuffers[iOutput ^ 1];
			outbuffer = (CLBuffer<O>) tempBuffers[iOutput];
			if (outbuffer == null) {
				tempBuffers[iOutput] = BufferHelper.createInputOutputBufferFor(
						ctx, getOutputType(), blocksInCurrentDepth);
				outbuffer = tempBuffers[iOutput];
			}
			synchronized (kernel) {
				if (gen instanceof MapReduceCodeGen) {
					kernel.setArgs(inbuffer, currentInput, outbuffer,
							(long) current_size, (long) blocksInCurrentDepth,
							(long) DEFAULT_MAX_REDUCTION_SIZE, (depth == 0) ? 1 : 0);
				} else {
					kernel.setArgs(currentInput, outbuffer, (long) current_size,
						(long) blocksInCurrentDepth,
						(long) DEFAULT_MAX_REDUCTION_SIZE);
				}
				setExtraDataArgs(kernel);
				int workgroupSize = blocksInCurrentDepth;
				if (workgroupSize == 1)
					workgroupSize = 2;
				blockCountArr[0] = workgroupSize;
				eventsArr[0] = kernel.enqueueNDRange(q, blockCountArr, null,
						eventsArr);
			}
			
			// debugBuffers(ctx, q, "in", currentInput, current_size);
			// debugBuffers(ctx, q, "out", outbuffer, blocksInCurrentDepth);
			
			current_size = blocksInCurrentDepth;
			depth++;
		}

		kernelCompletion = eventsArr[eventsArr.length - 1];

	}

	@SuppressWarnings("unchecked")
	public void debugBuffers(CLContext ctx, CLQueue q, String bufname, CLBuffer<?> buf, int size) {
		PList<O> li;

		System.out.println("buf:" + bufname);
		li = (PList<O>) BufferHelper.extractFromBuffer(buf, q,
				kernelCompletion, getOutputType(), end);
		for (int i = 0; i < size; i++) {
			System.out.print(li.get(i) + ",");
		}
		System.out.println("___");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		output = (O) BufferHelper.extractElementFromBuffer(outbuffer, q,
				kernelCompletion, getOutputType());
	}

	@Override
	public void release() {
		this.inbuffer.release();
		// this.outbuffer.release();
		super.release();
	}
	
	public O getOutput() {
		return output;
	}

	// Helper Methods

	private void inferBestValues() {
		int max_threads = 16;
		int max_blocks = (int) device.getDevice().getMaxWorkGroupSize()
				/ max_threads;
		int n = input.size();

		if (n < max_threads) {
			threads = n;
			blocks = 1;
		} else {
			threads = max_threads;
			blocks = (n * 2 - 1) / (threads * 2);
			blocks = (blocks > max_blocks) ? max_blocks : blocks;
		}
	}

	static int getNextPowerOfTwo(int i) {
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
	

	public LambdaReducerWithSeed<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaReducerWithSeed<O> reduceFun) {
		this.reduceFun = reduceFun;
	}
	
	public String getOpenCLSeed() {
		return reduceFun.getSeedSource();
	}

	public String getInputType() {
		return input.getContainingType().getSimpleName().toString();
	}

	public String getOutputType() {
		return ExtractTypes.extractReturnTypeOutOf(reduceFun, "combine");
	}

	public int getOutputSize() {
		return input.size();
	}

	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}

	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}

}
