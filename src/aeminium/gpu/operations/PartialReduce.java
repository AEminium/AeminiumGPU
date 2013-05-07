package aeminium.gpu.operations;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaNoSeedReducer;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.generator.ReduceCodeGen;
import aeminium.gpu.operations.generator.ReduceTemplateSource;
import aeminium.gpu.operations.utils.ExtractTypes;
import aeminium.gpu.operations.utils.FeatureHelper;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class PartialReduce<O> extends GenericProgram implements Program,
		ReduceTemplateSource<O> {

	static final int DEFAULT_MAX_REDUCTION_SIZE = 4;

	protected PList<O> input;
	protected PList<O> output;
	protected int outputSize;
	protected LambdaNoSeedReducer<O> reduceFun;

	protected CLBuffer<?> inbuffer;
	private CLBuffer<?> outbuffer;

	private ReduceCodeGen gen;

	// Constructors

	public PartialReduce(LambdaNoSeedReducer<O> reduceFun2, PList<O> list,
			int outputSize, GPUDevice dev) {
		this(reduceFun2, list, outputSize, "", dev);
	}

	public PartialReduce(LambdaNoSeedReducer<O> reduceFun, PList<O> list,
			int outputSize, String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.outputSize = outputSize;
		this.reduceFun = reduceFun;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
		if (list instanceof Range) {
			gen.setRange(true);
		}
		if (reduceFun instanceof LambdaReducer) {
			gen.setHasSeed(true);
		}
	}

	// only for subclasses
	protected PartialReduce(LambdaNoSeedReducer<O> reduceFun2, GPUDevice dev) {
		this(reduceFun2, "", dev);
	}

	protected PartialReduce(LambdaNoSeedReducer<O> reduceFun2, String other,
			GPUDevice dev) {
		this.device = dev;
		this.reduceFun = reduceFun2;
		this.setOtherSources(other);
		gen = new ReduceCodeGen(this);
	}

	protected boolean willRunOnGPU() {
		return OpenCLDecider.useGPU(input.size(), this.getFeatures());
	}

	@SuppressWarnings("unchecked")
	public void cpuExecution() {
		output = (PList<O>) CollectionFactory.listFromType(getOutputType());
		if (reduceFun instanceof LambdaReducer) {
			LambdaReducer<O> red = (LambdaReducer<O>) reduceFun; 
			O acc = red.getSeed();
			for (int i = 0; i < input.size(); i++) {
				acc = reduceFun.combine(acc, input.get(i));
				if ((i + 1) % outputSize == 0) {
					output.add(acc);
					acc = red.getSeed();
				}
			}
		} else {
			O acc = input.get(0);
			for (int i = 1; i < input.size(); i++) {
				acc = reduceFun.combine(acc, input.get(i));
				if ((i + 1) % outputSize == 0) {
					output.add(acc);
					acc = input.get(i++);
				}
			}
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
		inbuffer = BufferHelper.createInputBufferFor(ctx, input);
		outbuffer = BufferHelper.createInputOutputBufferFor(ctx,
				getOutputType(), outputSize);
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		synchronized (kernel) {
			kernel.setArgs(inbuffer, outbuffer, (long) input.size(),
					(long) outputSize, (long) (input.size() / outputSize));
			kernelCompletion = kernel.enqueueNDRange(q,
					new int[] { outputSize }, null, new CLEvent[] {});
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		output = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q,
				kernelCompletion, getOutputType(), outputSize);
	}

	@Override
	public void release() {
		this.inbuffer.release();
		this.outbuffer.release();
		super.release();
	}

	// Output

	public PList<O> getOutput() {
		// No need for lazyness in reduces.
		execute();
		return output;
	}

	// Utils

	public String getOpenCLSeed() {
		if (reduceFun instanceof LambdaReducer) {
			return ((LambdaReducer<O>)reduceFun).getSeedSource();
		} else {
			return "return 0;";
		}
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
		this.output = output;
	}

	public LambdaNoSeedReducer<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaNoSeedReducer<O> reduceFun) {
		this.reduceFun = reduceFun;
	}

	@Override
	public String getKernelName() {
		return gen.getReduceKernelName();
	}
	
	public String getFeatures() {
		return FeatureHelper.getFullFeatures(reduceFun.getFeatures(), input.size(),
				getInputType(), outputSize, getOutputType(), 2);
	}

}
