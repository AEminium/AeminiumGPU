package aeminium.gpu.operations;

import aeminium.gpu.backends.cpu.CPUPartialReduce;
import aeminium.gpu.backends.gpu.GPUPartialReduce;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.utils.ExtractTypes;

public class PartialReduce<O> extends GenericProgram {


	protected PList<O> input;
	protected PList<O> output;
	protected int outputSize;
	protected LambdaReducer<O> reduceFun;
	
	protected GPUPartialReduce<O> gpuOp;
	protected CPUPartialReduce<O> cpuOp;
	
	// Constructors

	public PartialReduce(LambdaReducer<O> reduceFun2, PList<O> list,
			int groupBy, GPUDevice dev) {
		this(reduceFun2, list, groupBy, "", dev);
	}

	public PartialReduce(LambdaReducer<O> reduceFun, PList<O> list,
			int groupBy, String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.outputSize = list.size() / groupBy;
		this.reduceFun = reduceFun;
		
		cpuOp = new CPUPartialReduce<O>(input, reduceFun, outputSize);
		gpuOp = new GPUPartialReduce<O>(input, reduceFun, outputSize);
		gpuOp.setOtherSources(other);
		gpuOp.setDevice(dev);
	}
	
	@Override
	public int getParallelUnits() {
		return this.outputSize;
	}

	@Override
	protected int getBalanceSplitPoint() {
		return OpenCLDecider.getSplitPoint(getParallelUnits(), input.size(), outputSize,
				reduceFun.getSource(), reduceFun.getSourceComplexity());
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
			output.extend(cpuOp.getOutput());
		}
	}

	
	// Output

	public PList<O> getOutput() {
		// No need for lazyness in reduces.
		execute();
		return output;
	}

	// Utils

	public String getOpenCLSeed() {
		if (reduceFun instanceof LambdaReducerWithSeed) {
			return ((LambdaReducerWithSeed<O>) reduceFun).getSeedSource();
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

	public LambdaReducer<O> getReduceFun() {
		return reduceFun;
	}

	public void setReduceFun(LambdaReducer<O> reduceFun) {
		this.reduceFun = reduceFun;
	}

}
