package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.runtime.Runtime;
import aeminium.runtime.*;

import java.util.LinkedList;
import java.util.List;

public class MCPUFilter<T> extends MCPUGenericKernel {
    private static final int NTHREADS = java.lang.Runtime.getRuntime().availableProcessors();
    protected Task cTask;
    protected FilterBody cBody;

    protected PList<T> input;
    protected PList<T> output;
    protected LambdaFilter<T> filterFun;

    private final String type;

    public MCPUFilter(PList<T> input, LambdaFilter<T> filterFun) {
        this.input = input;
        this.filterFun = filterFun;
        this.type = input.getContainingType().getSimpleName();
    }

    public class FilterBody implements Body {
        private final boolean firstSchedule;
        public int start;
        public int end;
        public PList<T> output;

        public FilterBody(int start, int end, boolean firstSchedule) {
            this.start = start;
            this.end = end;
            this.firstSchedule = firstSchedule;
        }

        @SuppressWarnings("unchecked")
        public void sequentialExecute() {
            this.output = (PList<T>) CollectionFactory.listFromType(input.getContainingType().getSimpleName());
            for (int i = start; i < end; i++) {
                final T element = input.get(i);
                if (filterFun.filter(element)) {
                    output.add(element);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(Runtime rt, Task current) {

            if (firstSchedule && rt.parallelize(current) && end - start > 100) {
                List<Task> tasks = new LinkedList<>();
                List<FilterBody> bodies = new LinkedList<>();

                for (int i = 0; i < NTHREADS; i++) {
                    final int new_start = i * input.size() / NTHREADS;
                    final int new_end = (i + 1) * input.size() / NTHREADS;
                    final FilterBody body = new FilterBody(new_start, new_end, false);
                    bodies.add(body);
                    final NonBlockingTask task = rt.createNonBlockingTask(body, Hints.LOOPS);
                    tasks.add(task);
                    rt.schedule(task, Runtime.NO_PARENT, Runtime.NO_DEPS);
                }

                for (Task task : tasks) {
                    task.getResult();
                }

                this.output = (PList<T>) CollectionFactory.listFromType(input.getContainingType().getSimpleName());
                for (FilterBody body : bodies) {
                    assert output != null;
                    output.extend(body.output);
                }
            } else {
                sequentialExecute();
            }
        }
    }

    @Override
    public void execute() {
        cBody = new FilterBody(start, end, true);
        cTask = CPUDevice.submit(cBody);
        cTask.getResult();
    }

    @Override
    public void waitForExecution() {
        CPUDevice.waitFor(cTask);
    }

    public PList<T> getInput() {
        return input;
    }

    public void setInput(PList<T> input) {
        this.input = input;
    }

    public PList<T> getOutput() {
        return output;
    }

    public void setOutput(PList<T> output) {
        this.output = output;
    }

    public LambdaFilter<T> getFilterFun() {
        return filterFun;
    }

    public void setFilterFun(LambdaFilter<T> filterFun) {
        this.filterFun = filterFun;
    }

    public String getType() {
        return type;
    }
}
