package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;

public class MCPUReduce<I, O> extends MCPUGenericKernel {
	protected PList<I> input;
	protected O output;
	protected LambdaMapper<I, O> mapFun;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	protected Task cTask;
	protected ReducerBody cBody;
	
	public MCPUReduce(PList<I> input, LambdaReducerWithSeed<O> reduceFun) {
		this(input, new LambdaMapper<I, O>() {
			@SuppressWarnings("unchecked")
			@Override
			public O map(I input) {
				return (O) input;
			}
		}, reduceFun);
	}
	
	public MCPUReduce(PList<I> input, LambdaMapper<I,O> mapper, LambdaReducerWithSeed<O> reduceFun) {
		this.input = input;
		this.reduceFun = reduceFun;
		this.mapFun = mapper;
	}
	
	@Override
	public void execute() {
		cBody = cpuParallelReducer(start, end);
		cTask = CPUDevice.submit(cBody);
	}
	
	@Override
	public void waitForExecution() {
		CPUDevice.waitFor(cTask);
	}
	
	
	public class ReducerBody implements Body {
		public LambdaReducerWithSeed<O> reduceFun;
		public O output;
		public int start;
		public int end;
		
		public ReducerBody(LambdaReducerWithSeed<O> reduceFun, int start, int end) {
			this.start = start;
			this.end = end;
			this.reduceFun = reduceFun;
			this.output = reduceFun.getSeed();
		}
		
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			if ((end - start) > 4 && rt.parallelize(current)) {
				int s = start + (end - start)/2;
				ReducerBody b1 = cpuParallelReducer(start, s);
				Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
				ReducerBody b2 = cpuParallelReducer(s, end);
				Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);
				t1.getResult();
				t2.getResult();
				output = reduceFun.combine(b1.output, b2.output);
			} else {
				for (int i=start; i < end; i++) {
					output = reduceFun.combine(mapFun.map(input.get(i)), output);
				}
			}
		}
	}
	
	public ReducerBody cpuParallelReducer(final int start, final int end) {
		return new ReducerBody(reduceFun, start, end);
	}

	public O getOutput() {
		return cBody.output;
	}

	public void setOutput(O output) {
		this.output = output;
	}

}
