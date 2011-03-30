package aeminium.gpu.lists.properties;

import aeminium.gpu.operations.functions.LambdaReducer;

public interface Reductionable<T> {
	public T reduce(LambdaReducer<T> reducer);
}
