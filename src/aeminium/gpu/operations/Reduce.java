package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPUReduce;
import aeminium.gpu.backends.mcpu.MCPUReduce;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.contracts.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class Reduce<O> extends GenericProgram implements Program {

	protected PList<O> input;
	private O output;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	protected GPUReduce<O, O> gpuOp;
	protected MCPUReduce<O, O> cpuOp;

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
		
		cpuOp = new MCPUReduce<O, O>(input, reduceFun);
		gpuOp = new GPUReduce<O, O>(input, reduceFun);
		gpuOp.setOtherSources(other);
		gpuOp.setDevice(dev);
	}

	// only for subclasses
	protected Reduce(LambdaReducerWithSeed<O> reduceFun2, GPUDevice dev) {
		this(reduceFun2, "", dev);
	}

	protected Reduce(LambdaReducerWithSeed<O> reduceFun2, String other,
			GPUDevice dev) {
		this(reduceFun2, null, other, dev);
	}

	
	
	@Override
	public int getParallelUnits() {
		return this.input.size();
	}
	
	@Override
	protected int getBalanceSplitPoint() {
		return OpenCLDecider.getSplitPoint(getParallelUnits(), input.size(), 1,
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
			output = reduceFun.combine(output, cpuOp.getOutput());
		}
	}

	// Output
	public O getOutput() {
		execute(); // No need for lazyness in reduces.
		return output;
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
	
	public GPUReduce<O, O> getGPUReduce() {
		return gpuOp;
	}
}
