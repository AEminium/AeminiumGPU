package aeminium.gpu.backends.mcpu;

import java.util.ArrayList;
import java.util.List;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;

public class MCPURecursiveCall<R, A> extends MCPUGenericKernel {
	public PList<A> args;
	public R output;
	public final BinaryRecursiveStrategy<R, A> strategy;
	
	List<RecursiveCallBody<R, A>> bodies = new ArrayList<RecursiveCallBody<R, A>>();
	List<Task> tasks = new ArrayList<Task>();
	
	public MCPURecursiveCall(BinaryRecursiveStrategy<R, A> recursiveStrategy) {
		strategy = recursiveStrategy;
	}
	
	public void setArgs(PList<A> a) {
		args = a;
	}
	
	@Override
	public void execute() {
		for(A arg : args.subList(start, end)) {
			RecursiveCallBody<R, A> b = new RecursiveCallBody<R, A>(this, arg, strategy.getSeed());
			bodies.add(b);
			tasks.add(CPUDevice.submit(b));
		}
	}

	@Override
	public void waitForExecution() {
		for (Task t : tasks) {
			CPUDevice.waitFor(t);
		}
	}

	public R getOutput() {
		R acc = strategy.getSeed();
		for (RecursiveCallBody<R, A> b : bodies) {
			acc = strategy.combine(acc, b.acc);
		}
		return acc;
	}
	
	public static class RecursiveCallBody<R, A> implements Body, RecursiveCallback {
		public R acc;
		public A arg;
		MCPURecursiveCall<R, A> op;
		boolean isDone = false;
		
		public RecursiveCallBody(MCPURecursiveCall<R, A> op, A arg, R acc) {
			this.op = op;
			this.acc = acc;
			this.arg = arg;
		}
		
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			R itacc = op.strategy.call(arg, this);
			if (!isDone) {
				PList<A> next = op.strategy.split(arg, itacc);
				List<RecursiveCallBody<R, A>> bodies = new ArrayList<RecursiveCallBody<R, A>>();
				List<Task> tasks = new ArrayList<Task>();
				
				for (A a: next) {
					RecursiveCallBody<R, A> body = new RecursiveCallBody<R, A>(op, a, itacc);
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
				for (RecursiveCallBody<R, A> b : bodies) {
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
