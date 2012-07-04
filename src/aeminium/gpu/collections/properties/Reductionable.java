package aeminium.gpu.collections.properties;

import aeminium.gpu.operations.functions.LambdaNoSeedReducer;

public interface Reductionable<T> {
	public T reduce(LambdaNoSeedReducer<T> reducer);
}
