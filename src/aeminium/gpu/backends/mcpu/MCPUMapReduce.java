package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class MCPUMapReduce<I, O> extends MCPUAbstractReduce {
    protected PList<I> input;
    protected O output;
    protected LambdaMapper<I, O> mapFun;
    protected LambdaReducerWithSeed<O> reduceFun;

    public MCPUMapReduce(PList<I> input, LambdaReducerWithSeed<O> reduceFun) {
        this(input, new LambdaMapper<I, O>() {
            @SuppressWarnings("unchecked")
            @Override
            public O map(I input) {
                return (O) input;
            }
        }, reduceFun);
    }

    public MCPUMapReduce(PList<I> input, LambdaMapper<I, O> mapper, LambdaReducerWithSeed<O> reduceFun) {
        this.input = input;
        this.reduceFun = reduceFun;
        this.mapFun = mapper;
    }


    public class ReducerBody extends AbstractReducerBody<O> {

        public ReducerBody(LambdaReducerWithSeed<O> reduceFun, int start, int end) {
            super(reduceFun, start, end);
        }

        @Override
        public void sequentialExecute() {
            for (int i = start; i < end; i++) {
                output = reduceFun.combine(mapFun.map(input.get(i)), output);
            }
        }

    }

    public ReducerBody cpuParallelReducer(final int start, final int end) {
        return new ReducerBody(reduceFun, start, end);
    }

    public O getOutput() {
        return (O) cBody.output;
    }

    public void setOutput(O output) {
        this.output = output;
    }

}
