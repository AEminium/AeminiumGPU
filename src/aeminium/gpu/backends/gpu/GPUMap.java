package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.generators.MapCodeGen;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPUMap<I,O> extends GPUGenericKernel {
	
	protected PList<I> input;
	protected PList<O> output;
	protected LambdaMapper<I, O> mapFun;
	
	protected String outputType;
	
	protected CLBuffer<?> inbuffer;
	private CLBuffer<?> outbuffer;
	
	private MapCodeGen gen;
	
	public GPUMap(PList<I> input, LambdaMapper<I, O> mapFun, String otherSources) {
		this.input = input;
		this.mapFun = mapFun;
		outputType = mapFun.getOutputType();
		this.setOtherSources(otherSources);
		
		gen = new MapCodeGen(this);
		if (input instanceof Range) {
			gen.setRange(true);
		}
	}
	
	@Override
	public String getKernelName() {
		return gen.getMapKernelName();
	}
	
	@Override
	public String getSource() {
		return gen.getMapKernelSource();
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		inbuffer = BufferHelper.createInputBufferFor(ctx, input, end);
		outbuffer = BufferHelper.createOutputBufferFor(ctx, outputType,
				end);
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		synchronized (kernel) {
			// setArgs will throw an exception at runtime if the types / sizes
			// of the arguments are incorrect
			kernel.setArgs(inbuffer, outbuffer);

			// Ask for 1-dimensional execution of length dataSize, with auto
			// choice of local workgroup size :
			kernelCompletion = kernel.enqueueNDRange(q,
					new int[] { end }, new CLEvent[] {});
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		output = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q,
				kernelCompletion, outputType, end);
	}
	
	@Override
	public void release() {
		super.release();
		this.inbuffer.release();
		this.outbuffer.release();
	}
	
	public String getMapOpenCLSource() {
		return gen.getMapLambdaSource();
	}

	public String getMapOpenCLName() {
		return gen.getMapLambdaName();
	}
	
	
	// Utils
	
	public String getInputType() {
		return input.getType().getSimpleName().toString();
	}
	
	public void setOutputType(String ot) {
		outputType = ot;
	}
	
	public String getOutputType() {
		return outputType;
	}

	public PList<O> getOutput() {
		return output;
	}
	
	public PList<I> getInput() {
		return input;
	}


	public void setInput(PList<I> input) {
		this.input = input;
	}


	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}


	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}


	public void setOutput(PList<O> output) {
		this.output = output;
	}

	
}
