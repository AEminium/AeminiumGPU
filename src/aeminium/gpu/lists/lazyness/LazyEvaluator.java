package aeminium.gpu.lists.lazyness;

import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public interface LazyEvaluator<T> {
	public Object evaluate();
	public Class<?> getType();
	public <O> boolean canMergeWithMap(LambdaMapper<T,O> mapFun);
	public <O> PList<O> mergeWithMap(Map<T,O> mapOp);
	
	public boolean canMergeWithReduce(LambdaReducer<T> reduceFun);
	public T mergeWithReducer(Reduce<T> reduceOp);

 }
