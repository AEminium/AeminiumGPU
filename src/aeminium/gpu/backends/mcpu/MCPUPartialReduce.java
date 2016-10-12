package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;

public class MCPUPartialReduce<O> extends MCPUGenericKernel {
	protected PList<O> input;
	protected PList<O> output;
	protected LambdaReducer<O> reduceFun;
	
	protected Task cTask;
	
	protected int outputSize;

	public MCPUPartialReduce(PList<O> input,
			LambdaReducer<O> reduceFun, int outputSize) {
		this.input = input;
		this.reduceFun = reduceFun;
		this.outputSize = outputSize;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		System.out.println("t:" + input.getContainingType().toString());
		output = (PList<O>) CollectionFactory.listFromType(input.getContainingType().toString());
		if (end > start) {
			output.set(end-1-start, input.get(0));
		}
		cTask = ForTask.createFor(CPUDevice.rt, new aeminium.runtime.helpers.loops.Range(start, end), new ForBody<Integer>() {

			@Override
			public void iterate(Integer line, aeminium.runtime.Runtime rt,
					Task current) {
				O acc;
				if (reduceFun instanceof LambdaReducerWithSeed) {
					LambdaReducerWithSeed<O> red = (LambdaReducerWithSeed<O>) reduceFun;
					acc = red.getSeed();
					for (int i = line * outputSize; i < (line+1) * outputSize; i++) {
						acc = reduceFun.combine(acc, input.get(i));
					}
				} else {
					acc = input.get(line * outputSize);
					for (int i = line * outputSize + 1; i < (line+1) * outputSize; i++) {
						acc = reduceFun.combine(acc, input.get(i));
					}
				}
				output.set(line-start, acc);
			}
			
		}, Runtime.NO_HINTS);
		CPUDevice.submit(cTask);
	}
	
	@Override
	public void waitForExecution() {
		CPUDevice.waitFor(cTask);
	}
	
	public String getOutputType() {
		return input.getContainingType().toString();
	}
	
	public PList<O> getOutput() {
		return output;
	}

	
}
