package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.gpu.operations.functions.RecursiveStrategy;
import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;

public class MCPURecursive<R extends Number, T> extends MCPUGenericKernel {

	public T output;
	public final RecursiveStrategy<R, T> strategy;
	public Task cTask;
	public RecursiveBody<R, T> cBody;
	
	
	public MCPURecursive(RecursiveStrategy<R, T> recursiveStrategy) {
		strategy = recursiveStrategy;
	}

	@Override
	public void execute() {
		
		cBody = new RecursiveBody<R, T>(this, strategy.getSeed(), strategy.getStart(), strategy.getEnd());
		cTask = CPUDevice.submit(cBody);
	}

	@Override
	public void waitForExecution() {
		CPUDevice.waitFor(cTask);
	}

	public T getOutput() {
		return cBody.acc;
	}
	
	public static class RecursiveBody<R extends Number, T> implements Body, RecursiveCallback {

		public T acc;
		R st;
		R end;
		boolean isDone;
		MCPURecursive<R, T> op;
		
		public RecursiveBody(MCPURecursive<R, T> op, T accumulator, R start, R end) {
			this.acc = accumulator;
			this.st = start;
			this.end = end;
			this.op = op;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			T itacc = op.strategy.iterative(st, end, this);
			if (!isDone) {
				PList<R> ranges = (PList<R>) CollectionFactory.listFromType(st.getClass().getSimpleName());
				op.strategy.split(ranges, 0, st, end, 2);
			
				RecursiveBody<R, T> leftBody = new RecursiveBody<R, T>(op, itacc, ranges.get(0), ranges.get(1));
				RecursiveBody<R, T> rightBody = new RecursiveBody<R, T>(op, itacc, ranges.get(1), end);
				
				if (rt.parallelize(current) && rt.getTaskCount() < 30) {			
					Task leftSide = rt.createNonBlockingTask(leftBody, (short) (Hints.RECURSION));
					rt.schedule(leftSide, current, Runtime.NO_DEPS);
					
					Task rightSide = rt.createNonBlockingTask(rightBody, (short) (Hints.RECURSION));
					rt.schedule(rightSide, current, Runtime.NO_DEPS);
		
					leftSide.getResult();
					rightSide.getResult();
				} else {
					leftBody.execute(rt, current);
					rightBody.execute(rt, current);
				}
				
				itacc = op.strategy.combine(rightBody.acc, leftBody.acc);
			}
			acc = itacc;
		}

		@Override
		public void markDone() {
			isDone = true;
		}
	}
	
}
