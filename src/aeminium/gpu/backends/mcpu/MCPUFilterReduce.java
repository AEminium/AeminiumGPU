package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class MCPUFilterReduce<T> extends MCPUAbstractReduce {
    protected PList<T> input;
    protected T output;
    protected LambdaFilter<T> filterFun;
    protected LambdaReducerWithSeed<T> reduceFun;

    public MCPUFilterReduce(PList<T> input, LambdaReducerWithSeed<T> reduceFun) {
        this(input, new LambdaFilter<T>() {
            @Override
            public boolean filter(T input) {
                return true;
            }
        }, reduceFun);
    }

    public MCPUFilterReduce(PList<T> input, LambdaFilter<T> filter, LambdaReducerWithSeed<T> reduceFun) {
        this.input = input;
        this.reduceFun = reduceFun;
        this.filterFun = filter;
    }


    public class ReducerBody extends AbstractReducerBody<T> {
        ReducerBody(LambdaReducerWithSeed<T> reduceFun, int start, int end) {
            super(reduceFun, start, end);
        }

        @Override
        public void sequentialExecute() {
            for (int i = start; i < end; i++) {
                if (filterFun.filter(input.get(i))) {
                    output = reduceFun.combine(input.get(i), output);
                }
            }
        }
    }

    public ReducerBody cpuParallelReducer(final int start, final int end) {
        return new ReducerBody(reduceFun, start, end);
    }

    public T getOutput() {
        return (T) cBody.output;
    }

    public void setOutput(T output) {
        this.output = output;
    }
}
