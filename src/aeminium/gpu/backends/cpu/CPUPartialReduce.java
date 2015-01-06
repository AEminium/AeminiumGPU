package aeminium.gpu.backends.cpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class CPUPartialReduce<O> extends CPUGenericKernel {
	protected PList<O> input;
	protected PList<O> output;
	protected LambdaReducer<O> reduceFun;

	protected int outputSize;

	public CPUPartialReduce(PList<O> input, LambdaReducer<O> reduceFun,
			int outputSize) {
		this.input = input;
		this.reduceFun = reduceFun;
		this.outputSize = outputSize;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		output = (PList<O>) CollectionFactory.listFromType(getOutputType());
		O acc;
		for (int line = start; line < end; line++) {
			if (reduceFun instanceof LambdaReducerWithSeed) {
				LambdaReducerWithSeed<O> red = (LambdaReducerWithSeed<O>) reduceFun;
				acc = red.getSeed();
				for (int i = line * outputSize; i < (line + 1) * outputSize; i++) {
					acc = reduceFun.combine(acc, input.get(i));
				}
			} else {
				acc = input.get(line * outputSize);
				for (int i = line * outputSize + 1; i < (line + 1) * outputSize; i++) {
					acc = reduceFun.combine(acc, input.get(i));
				}
			}
			output.set(line - start, acc);
		}
	}

	@Override
	public void waitForExecution() {
	}

	public String getOutputType() {
		return input.getContainingType().getSimpleName().toString();
	}

	public PList<O> getOutput() {
		return output;
	}

}
