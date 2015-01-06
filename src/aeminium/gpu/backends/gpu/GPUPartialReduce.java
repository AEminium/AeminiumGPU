package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.ReduceCodeGen;
import aeminium.gpu.backends.gpu.generators.ReduceTemplateSource;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPUPartialReduce<O> extends GPUGenericKernel implements ReduceTemplateSource<O> {
	
	static final int DEFAULT_MAX_REDUCTION_SIZE = 4;
	
	protected PList<O> input;
	protected PList<O> output;
	protected LambdaReducer<O> reduceFun;
	protected int outputSize;
	
	protected CLBuffer<?> inbuffer;
	private CLBuffer<?> outbuffer;

	private ReduceCodeGen gen;
	
	public GPUPartialReduce(PList<O> input, LambdaReducer<O> reduceFun, int outputSize) {
		this.input = input;
		this.reduceFun = reduceFun;
		this.outputSize = outputSize;
		gen = new ReduceCodeGen(this);
		if (reduceFun instanceof LambdaReducerWithSeed) {
			gen.setHasSeed(true);
		}
		if (input instanceof Range) {
			gen.setRange(true);
		}
		otherData = OtherData.extractOtherData(reduceFun);
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
		super.prepareBuffers(ctx);
		inbuffer = BufferHelper.createInputBufferFor(ctx, input, input.size());
		outbuffer = BufferHelper.createInputOutputBufferFor(ctx,
				getOutputType(), outputSize);
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		synchronized (kernel) {
			kernel.setArgs(inbuffer, outbuffer, (long) input.size(),
					(long) end, (long) (input.size() / outputSize));
			setExtraDataArgs(kernel);
			kernelCompletion = kernel.enqueueNDRange(q,
					new int[] { end }, null, new CLEvent[] {});
		}
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
		output = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q,
				kernelCompletion, getOutputType(), end);
	}

	@Override
	public void release() {
		this.inbuffer.release();
		this.outbuffer.release();
		super.release();
	}
	
	public PList<O> getOutput() {
		return output;
	}

	// Helper Methods	

	public String getOpenCLSeed() {
		if (reduceFun instanceof LambdaReducerWithSeed) {
			return ((LambdaReducerWithSeed<O>) reduceFun).getSeedSource();
		} else {
			return "return 0;";
		}
	}
	
	public LambdaReducer<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaReducerWithSeed<O> reduceFun) {
		this.reduceFun = reduceFun;
	}

	public String getInputType() {
		return input.getContainingType().getSimpleName().toString();
	}

	public String getOutputType() {
		return input.getContainingType().getSimpleName().toString();
	}

	public int getOutputSize() {
		return input.size();
	}
}
