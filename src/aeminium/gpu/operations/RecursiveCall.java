package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPURecursiveCall;
import aeminium.gpu.backends.mcpu.MCPURecursiveCall;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.contracts.Program;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;

public class RecursiveCall<R, A> extends GenericProgram implements Program {

	BinaryRecursiveStrategy<R, A> strategy;
	protected R output;
	protected A arg;
	
	protected GPURecursiveCall<R, A> gpuOp;
	protected MCPURecursiveCall<R, A> cpuOp;
	
	protected PList<A> futureArgs;
	
	public RecursiveCall(BinaryRecursiveStrategy<R, A> binaryRecursiveStrategy) {
		strategy = binaryRecursiveStrategy;
		arg = binaryRecursiveStrategy.getArgument();
		device = binaryRecursiveStrategy.getDevice();
		cpuOp = new MCPURecursiveCall<R, A>(binaryRecursiveStrategy);
		gpuOp = new GPURecursiveCall<R, A>(binaryRecursiveStrategy);
		gpuOp.setDevice(device);
	}
	
	public R getOutput() {
		execute();
		return output;
	}
	
	@Override
	protected int getParallelUnits() {
		
		futureArgs = strategy.split(arg);
		for (int i=0; i < 10; i++) {
			if (futureArgs.size() <= 0) break;
			A o = futureArgs.remove(0);
			PList<A> more = strategy.split(o);
			futureArgs.add(more.get(0));
			futureArgs.add(more.get(1));
		}
		
		cpuOp.setArgs(futureArgs);
		gpuOp.setArgs(futureArgs);
		return futureArgs.size();
	}

	@Override
	protected int getBalanceSplitPoint() {
		return futureArgs.size() / 2;
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
