package aeminium.gpu.backends.cpu;

import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.gpu.operations.functions.Recursive1DStrategy;

public class CPURecursive<R extends Number,T> extends CPUGenericKernel implements RecursiveCallback {

	public T output;
	public final Recursive1DStrategy<R, T> strategy;
	public R start;
	public R end;
	public boolean isDone = false;
	
	
	public CPURecursive(Recursive1DStrategy<R, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		output = strategy.getSeed();
		start = strategy.getStart();
		end = strategy.getEnd();
	}
	
	public CPURecursive(Recursive1DStrategy<R, T> recursiveStrategy, R start, R end, T acc) {
		strategy = recursiveStrategy;
		this.start = start;
		this.end = end;
		this.output = acc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		// TODO: Equal to GPU
	}

	@Override
	public void waitForExecution() {
		// Do nothing
	}

	public T getOutput() {
		return output;
	}

	@Override
	public void markDone() {
		isDone = true;
	}

}
