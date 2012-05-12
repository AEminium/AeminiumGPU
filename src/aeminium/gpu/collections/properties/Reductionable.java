package aeminium.gpu.collections.properties;

import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public interface Reductionable<T> {
	public T reduce(LambdaReducerWithSeed<T> reducer);
}
