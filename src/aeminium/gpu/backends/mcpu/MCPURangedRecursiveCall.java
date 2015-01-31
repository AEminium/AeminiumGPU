package aeminium.gpu.backends.mcpu;

import java.util.ArrayList;
import java.util.List;

import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.Recursive2DStrategy;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;

public class MCPURangedRecursiveCall<R extends Number, R2, T> extends MCPUGenericKernel {

	public T output;
	public final Recursive2DStrategy<R, R2, T> strategy;
	public Task cTask;
	public RecursiveBody<R, R2, T> cBody;
	
	
	public MCPURangedRecursiveCall(Recursive2DStrategy<R, R2, T> recursiveStrategy) {
		strategy = recursiveStrategy;
	}

	@Override
	public void execute() {
		cBody = new RecursiveBody<R, R2,  T>(this, strategy.getSeed(), strategy.getStart(), strategy.getEnd(), strategy.getTop(), strategy.getBottom());
		cTask = CPUDevice.submit(cBody);
	}

	@Override
	public void waitForExecution() {
		CPUDevice.waitFor(cTask);
	}

	public T getOutput() {
		return cBody.acc;
	}
	
	public static class RecursiveBody<R extends Number, R2, T> implements Body, RecursiveCallback {

		public T acc;
		R st;
		R end;
		R2 top;
		R2 bottom;
		boolean isDone;
		MCPURangedRecursiveCall<R, R2, T> op;
		
		public RecursiveBody(MCPURangedRecursiveCall<R, R2, T> op, T accumulator, R start, R end, R2 top, R2 bottom) {
			this.acc = accumulator;
			this.st = start;
			this.end = end;
			this.top = top;
			this.bottom = bottom;
			this.op = op;
		}

		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			T itacc = op.strategy.iterative(st, end, top, bottom, this);
			if (!isDone) {
				Range2D<R, R2> ranges = op.strategy.split(st, end, top, bottom, 2);
			
				List<RecursiveBody<R, R2, T>> bodies = new ArrayList<RecursiveBody<R, R2, T>>();
				List<Task> tasks = new ArrayList<Task>();
				
				for (int i=0; i < ranges.size(); i++) {
					RecursiveBody<R, R2, T> body = new RecursiveBody<R, R2, T>(op, itacc, ranges.starts.get(i), ranges.ends.get(i), (ranges.tops != null) ? ranges.tops.get(i) : null,  (ranges.bottoms != null) ? ranges.bottoms.get(i) : null);
					bodies.add(body);
					
					if (rt.parallelize(current) && rt.getTaskCount() < 30) {
						Task t = rt.createNonBlockingTask(body, (short) (Hints.RECURSION));
						rt.schedule(t, current, Runtime.NO_DEPS);
						tasks.add(t);
					} else {
						body.execute(rt, current);
					}
				}
				
				for (Task t : tasks) {
					t.getResult();
				}
				itacc = op.strategy.getSeed();
				for (RecursiveBody<R, R2, T> b : bodies) {
					itacc = op.strategy.combine(itacc, b.acc);
				}
			}
			acc = itacc;
		}

		@Override
		public void markDone() {
			isDone = true;
		}
	}
	
}
