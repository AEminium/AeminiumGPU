package aeminium.gpu.lists.lazyness;

import aeminium.gpu.lists.PList;
import aeminium.gpu.lists.properties.operations.Mapper;
import aeminium.gpu.operations.Map;

public interface LazyEvaluator<T> {
	public Object evaluate();
	public Class<?> getType();
	public <O> boolean canMergeWithMap(Mapper<T,O> mapFun);
	public <O> PList<O> mergeWithMap(Map<T,O> mapOp);

 }
