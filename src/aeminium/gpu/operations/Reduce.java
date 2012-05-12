package aeminium.gpu.operations;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.generator.ReduceCodeGen;
import aeminium.gpu.operations.generator.ReduceTemplateSource;
import aeminium.gpu.operations.utils.ExtractTypes;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class Reduce<O> extends GenericProgram implements Program,
		ReduceTemplateSource<O> {

	static final int DEFAULT_MAX_REDUCTION_SIZE = 4;

	protected PList<O> input;
	private O output;
	protected LambdaReducerWithSeed<O> reduceFun;

	protected CLBuffer<?> inbuffer;
	private CLBuffer<?> outbuffer;

	private ReduceCodeGen gen;

	private int blocks;
	private int threads;
	private int current_size;

	// Constructors

	public Reduce(LambdaReducerWithSeed<O> reduceFun2, PList<O> list,
			GPUDevice dev) {
		this(reduceFun2, list, "", dev);
	}

	public Reduce(LambdaReducerWithSeed<O> reduceFun, PList<O> list,
			String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.reduceFun = reduceFun;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
		if (reduceFun instanceof LambdaReducerWithSeed) {
			gen.setHasSeed(true);
		}
	}

	// only for subclasses
	protected Reduce(LambdaReducerWithSeed<O> reduceFun2, GPUDevice dev) {
		this(reduceFun2, "", dev);
	}

	protected Reduce(LambdaReducerWithSeed<O> reduceFun2, String other,
			GPUDevice dev) {
		this.device = dev;
		this.reduceFun = reduceFun2;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
	}

	protected boolean willRunOnGPU() {
		return OpenCLDecider.useGPU(input.size(), 1, reduceFun.getSource(),
				reduceFun.getSourceComplexity());
	}

	public void cpuExecution() {
		output = this.getReduceFun().getSeed();
		for (int i = 0; i < input.size(); i++) {
			output = reduceFun.combine(input.get(i), output);
		}
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
		inbuffer = BufferHelper.createInputOutputBufferFor(ctx, input);
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
			CLBuffer<?> currentInput = (depth == 0) ? inbuffer
					: tempBuffers[iOutput ^ 1];
			outbuffer = (CLBuffer<O>) tempBuffers[iOutput];
			if (outbuffer == null) {
				tempBuffers[iOutput] = BufferHelper.createInputOutputBufferFor(
						ctx, getOutputType(), blocksInCurrentDepth);
				outbuffer = tempBuffers[iOutput];
			}
			synchronized (kernel) {
				kernel.setArgs(currentInput, outbuffer, (long) current_size,
						(long) blocksInCurrentDepth,
						(long) DEFAULT_MAX_REDUCTION_SIZE);
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
	public void debugBuffers(CLContext ctx, CLQueue q) {
		PList<O> li;

		System.out.println("i:");
		li = (PList<O>) BufferHelper.extractFromBuffer(inbuffer, q,
				kernelCompletion, getOutputType(), input.size());
		for (int i = 0; i < 35; i++) {
			System.out.print(li.get(i) + ",");
		}
		System.out.println("___");

		System.out.println("o:");
		li = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q,
				kernelCompletion, getOutputType(), input.size());
		for (int i = 0; i < 35; i++) {
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

	@Override
	public String getKernelName() {
		return gen.getReduceKernelName();
	}

}
