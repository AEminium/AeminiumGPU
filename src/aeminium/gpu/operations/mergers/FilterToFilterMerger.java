package aeminium.gpu.operations.mergers;

import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lazyness.LazyPList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Filter;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class FilterToFilterMerger<T> {

    private Filter<T> first;
    private Filter<T> second;
    private PList<T> current;

    public FilterToFilterMerger(Filter<T> first, Filter<T> second, PList<T> current) {
        this.first = first;
        this.second = second;
        this.current = current;
    }

    public PList<T> getOutput() {
        LazyEvaluator<T> eval = new LazyEvaluator<T>() {
            private Filter<T> fakeFilter() {
                LambdaFilter<T> fakeLambda = new LambdaFilter<T>() {
                    @Override
                    public boolean filter(T input) {
                        return first.getFilterFun().filter(input) && second.getFilterFun().filter(input);
                    }

                    @Override
                    public String getSource() {
                        return String.format("return %s(input) && %s(input);",
                                first.getGpuFilter().getFilterOpenCLName(),
                                second.getGpuFilter().getFilterOpenCLName());
                    }
                };

                StringBuilder extraCode = new StringBuilder();
                extraCode.append(first.getGpuFilter().getOtherSources());
                extraCode.append(second.getGpuFilter().getOtherSources());
                extraCode.append(first.getGpuFilter().getFilterOpenCLSource());
                extraCode.append(second.getGpuFilter().getFilterOpenCLSource());
                return new Filter<>(fakeLambda, current, extraCode.toString(), first.getDevice());
            }

            @Override
            public Object evaluate() {
                return fakeFilter().getOutput();
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
//                TODO:
                return null;
            }

            @Override
            public boolean canMergeWithReduce(LambdaReducerWithSeed<T> reduceFun) {
                return true;
            }

            @Override
            public T mergeWithReducer(Reduce<T> reduceOp) {
                FilterToReduceMerger<T> merger = new FilterToReduceMerger<T>(fakeFilter(), reduceOp, current);
                return merger.getOutput();
            }

            @Override
            public boolean canMergeWithFilter(LambdaFilter<T> filterFun) {
                return true;
            }

            @Override
            public PList<T> mergeWithFilter(Filter<T> filterOp) {
                return new FilterToFilterMerger<T>(fakeFilter(), filterOp, current).getOutput();
            }
        };
//        TODO: OutputSize can differ
        return new LazyPList<>(eval, second.getOutputSize());
    }
}
