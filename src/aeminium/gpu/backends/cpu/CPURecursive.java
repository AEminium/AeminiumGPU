package aeminium.gpu.backends.cpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.gpu.operations.functions.RecursiveStrategy;

public class CPURecursive<R,T> extends CPUGenericKernel implements RecursiveCallback {

	public T output;
	public final RecursiveStrategy<R, T> strategy;
	public R start;
	public R end;
	public boolean isDone = false;
	
	
	public CPURecursive(RecursiveStrategy<R, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		output = strategy.getSeed();
		start = strategy.getStart();
		end = strategy.getEnd();
	}
	
	public CPURecursive(RecursiveStrategy<R, T> recursiveStrategy, R start, R end, T acc) {
		strategy = recursiveStrategy;
		this.start = start;
		this.end = end;
		this.output = acc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		R next = end;
		T result;
		
		PList<R> ranges = (PList<R>) CollectionFactory.listFromType(start.getClass().getSimpleName());
		while (true) {
			result = strategy.iterative(start, next, this);
			if (isDone) {
				if (next == end) {
					break;
				}
				isDone = false;
				output = strategy.combine(output, result);
				start = next;
				next = end;
			} else {
				ranges.clear();
				strategy.split(ranges, 0, start, next, 500);
				next = ranges.get(1);
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
