package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPURecursive;
import aeminium.gpu.backends.mcpu.MCPURecursive;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.contracts.Program;
import aeminium.gpu.operations.functions.RecursiveStrategy;

public class RecursiveOp<R extends Number, T> extends GenericProgram implements Program {
	
	public RecursiveStrategy<R, T> strategy;
	private T output;
	
	protected GPURecursive<R, T> gpuOp;
	protected MCPURecursive<R, T> cpuOp;

	public RecursiveOp(RecursiveStrategy<R, T> recursiveStrategy) {
		this.strategy = recursiveStrategy;
		this.device = recursiveStrategy.getDevice();
		
		cpuOp = new MCPURecursive<R, T>(recursiveStrategy);
		gpuOp = new GPURecursive<R, T>(recursiveStrategy);
		gpuOp.setDevice(device);
	}

	public T getOutput() {
		execute();
		return output;
	}

	@Override
	protected int getParallelUnits() {
		return 1024;
	}

	@Override
	protected int getBalanceSplitPoint() {
		return 1024;
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
			output = strategy.combine(output, cpuOp.getOutput());
		}
	}

	@Override
	public void cpuExecution(int start, int end) {
		cpuOp.setLimits(start, end);
		cpuOp.execute();
	}

	@Override
	public void gpuExecution(int start, int end) {
		gpuOp.setLimits(start, end);
		gpuOp.execute();
	}

}
