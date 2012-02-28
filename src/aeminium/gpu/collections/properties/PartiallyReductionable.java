package aeminium.gpu.collections.properties;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;

public interface PartiallyReductionable<T> extends Reductionable<T>{
	public PList<T> reduceLines(LambdaReducer<T> lambdaReducer);
}
