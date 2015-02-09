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
		if (strategy.getStart() instanceof Double) {
			executeDouble((Recursive1DStrategy<Double, T>) strategy);
		}
		if (strategy.getStart() instanceof Integer) {
			executeInt((Recursive1DStrategy<Integer, T>) strategy);
		}
	}
	
	public void executeDouble(Recursive1DStrategy<Double, T> strat) {
		double s = start.doubleValue();
		double e = end.doubleValue();
		double step = e - s;
		while (s < e) {
			T a = strat.iterative(s, e, this);
			if (isDone) {
				output = strategy.combine(output, a);
				this.isDone = false;
				e = e + step;
				s = e;
			} else {
				step = step/2;
			}
		}
	}
	
	public void executeInt(Recursive1DStrategy<Integer, T> strat) {
		int s = start.intValue();
		int e = end.intValue();
		int step = e - s;
		while (s < e) {
			T a = strat.iterative(s, e, this);
			if (isDone) {
				output = strategy.combine(output, a);
				this.isDone = false;
				e = e + step;
				s = e;
			} else {
				step = step/2;
				if (step < 1) step = 1;
			}
		}
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
