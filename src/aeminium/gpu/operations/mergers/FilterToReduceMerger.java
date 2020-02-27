package aeminium.gpu.operations.mergers;

import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Filter;
import aeminium.gpu.operations.FilterReduce;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class FilterToReduceMerger<T> {
    private Filter<T> first;
    private Reduce<T> second;
    private PList<T> current;

    public FilterToReduceMerger(Filter<T> first, Reduce<T> second, PList<T> current) {
        this.first = first;
        this.second = second;
        this.current = current;
    }

    @SuppressWarnings("unchecked")
    public T getOutput() {
        LazyEvaluator<T> eval = new LazyEvaluator<T>() {
            @Override
            public Object evaluate() {
                StringBuilder extraCode = new StringBuilder();
                extraCode.append(first.getGpuFilter().getOtherSources());
                extraCode.append(second.getGPUReduce().getOtherSources());
                FilterReduce<T> op = new FilterReduce<T>(first.getFilterFun(),
                        second.getReduceFun(), current, extraCode.toString(),
                        first.getDevice());
                return op.getOutput();
            }

            @Override
            public Class<?> getType() {
                return current.getContainingType();
            }

            @Override
            public <O> boolean canMergeWithMap(LambdaMapper<T, O> mapFun) {
                return false;
            }

            @Override
            public <O> PList<O> mergeWithMap(Map<T, O> mapOp) {
                return null;
            }

            @Override
            public boolean canMergeWithReduce(LambdaReducerWithSeed<T> reduceFun) {
                return false;
            }

            @Override
            public T mergeWithReducer(Reduce<T> reduceOp) {
                return null;
            }

            @Override
            public boolean canMergeWithFilter(LambdaFilter<T> filterFun) {
                return false;
            }

            @Override
            public PList<T> mergeWithFilter(Filter<T> filterOp) {
                return null;
            }

        };
        return (T) eval.evaluate();
    }
}
