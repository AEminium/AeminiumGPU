package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPUReduce;
import aeminium.gpu.backends.mcpu.MCPUReduce;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.utils.FeatureHelper;

public class MapReduce<I, O> extends GenericProgram {

	protected PList<I> input;
	private O output;
	protected LambdaMapper<I, O> mapFun;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	protected GPUReduce<I, O> gpuOp;
	protected MCPUReduce<I, O> cpuOp;
	

	// Constructors
	public MapReduce(LambdaMapper<I, O> mapper,
			LambdaReducerWithSeed<O> reducer, PList<I> list, String other,
			GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.mapFun = mapper;
		this.reduceFun = reducer;
		
		cpuOp = new MCPUReduce<I, O>(input, mapFun, reduceFun);
		gpuOp = new GPUReduce<I, O>(input, mapFun, reduceFun);
		gpuOp.setOtherSources(other);
		gpuOp.setDevice(dev);
	}

	private String mergeComplexities(String one, String two) {
		if (one == null || one.length() == 0)
			return two;
		if (two == null || two.length() == 0)
			return one;
		return one + "+" + two;
	}

	@Override
	public int getParallelUnits() {
		return this.input.size();
	}

	@Override
	protected int getBalanceSplitPoint() {
		int s = OpenCLDecider.getSplitPoint(getParallelUnits(), input.size(),
				1,
				mapFun.getSource() + reduceFun.getSource(),
				mergeComplexities(mapFun.getSourceComplexity(),
						reduceFun.getSourceComplexity()));
		if (s < GPUReduce.DEFAULT_MAX_REDUCTION_SIZE) return 0;
		return s;
	}

	public void cpuExecution(int start, int end) {
		cpuOp.setLimits(start, end);
		cpuOp.execute();
	}

	@Override
	public void gpuExecution(int start, int end) {
		gpuOp.setLimits(start, end);
		gpuOp.execute();
	}
	
	@Override
	protected void mergeResults(boolean hasGPU, boolean hasCPU) {
		if (!hasGPU) {
			cpuOp.waitForExecution();
			output = cpuOp.getOutput();
		} else if (!hasCPU) {
			gpuOp.waitForExecution();
			output = gpuOp.getOutput();
		} else {
			gpuOp.waitForExecution();
			cpuOp.waitForExecution();
			output = gpuOp.getOutput();
			output = reduceFun.combine(output, cpuOp.getOutput());
		}
	}
	
	// Output

	public O getOutput() {
		// No need for lazyness in reduces.
		execute();
		return output;
	}

	// Utils

	public String getInputType() {
		return input.getContainingType().getSimpleName().toString();
	}

	public String getOutputType() {
		return input.getContainingType().getSimpleName().toString();
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
	
	public String getFeatures() {
		String sum = FeatureHelper.sumFeatures(mapFun.getFeatures(), reduceFun.getFeatures());
		return FeatureHelper.getFullFeatures(sum, input.size(), getInputType(), 1, getOutputType(), 3);
	}
}
