package aeminium.gpu.collections.lazyness;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Filter;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public interface LazyEvaluator<T> {
    Object evaluate();

    Class<?> getType();

    <O> boolean canMergeWithMap(LambdaMapper<T, O> mapFun);

    <O> PList<O> mergeWithMap(Map<T, O> mapOp);

    boolean canMergeWithReduce(LambdaReducerWithSeed<T> reduceFun);

    T mergeWithReducer(Reduce<T> reduceOp);

    boolean canMergeWithFilter(LambdaFilter<T> filterFun);

    PList<T> mergeWithFilter(Filter<T> filterOp);
}
