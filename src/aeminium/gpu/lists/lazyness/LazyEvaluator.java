package aeminium.gpu.lists.lazyness;

import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.functions.LambdaMapper;

public interface LazyEvaluator<T> {
	public Object evaluate();
	public Class<?> getType();
	public <O> boolean canMergeWithMap(LambdaMapper<T,O> mapFun);
	public <O> PList<O> mergeWithMap(Map<T,O> mapOp);

 }
