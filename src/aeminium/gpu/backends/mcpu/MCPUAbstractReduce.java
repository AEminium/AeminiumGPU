package aeminium.gpu.backends.mcpu;

import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;

public abstract class MCPUAbstractReduce extends MCPUGenericKernel {
    protected Task cTask;
    protected AbstractReducerBody cBody;


    @Override
    public void execute() {
        cBody = cpuParallelReducer(start, end);
        cTask = CPUDevice.submit(cBody);
    }

    @Override
    public void waitForExecution() {
        CPUDevice.waitFor(cTask);
    }

    public abstract AbstractReducerBody cpuParallelReducer(int start, int end);

    public abstract class AbstractReducerBody<T> implements Body {
        public int start;
        public int end;
        public T output;
        public LambdaReducerWithSeed<T> reduceFun;


        public AbstractReducerBody(LambdaReducerWithSeed<T> reduceFun, int start, int end) {
            this.start = start;
            this.end = end;
            this.reduceFun = reduceFun;
            this.output = reduceFun.getSeed();
        }

        public abstract void sequentialExecute();

        @Override
        public void execute(Runtime rt, Task current) {
            if ((end - start) > 4 && rt.parallelize(current)) {
                int s = start + (end - start) / 2;
                AbstractReducerBody b1 = cpuParallelReducer(start, s);
                Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
                rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
                AbstractReducerBody b2 = cpuParallelReducer(s, end);
                Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
                rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);
                t1.getResult();
                t2.getResult();
                output = reduceFun.combine((T) b1.output, (T) b2.output);
            } else {
                sequentialExecute();
            }
        }
    }
}
