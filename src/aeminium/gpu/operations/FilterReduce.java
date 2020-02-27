package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPUReduce;
import aeminium.gpu.backends.mcpu.MCPUFilterReduce;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.utils.FeatureHelper;

public class FilterReduce<T> extends GenericProgram {

    protected PList<T> input;
    private T output;
    protected LambdaFilter<T> filterFun;
    protected LambdaReducerWithSeed<T> reduceFun;

    protected GPUReduce<T, T> gpuOp;
    protected MCPUFilterReduce<T> cpuOp;

    // Constructors
    public FilterReduce(LambdaFilter<T> filter, LambdaReducerWithSeed<T> reducer,
                        PList<T> list, String other, GPUDevice dev) {
        this.device = dev;
        this.input = list;
        this.filterFun = filter;
        this.reduceFun = reducer;

        cpuOp = new MCPUFilterReduce<T>(input, filterFun, reduceFun);
        gpuOp = new GPUReduce<T, T>(input, filterFun, reduceFun);
        gpuOp.setOtherSources(other);
        gpuOp.setDevice(dev);
    }

    private String mergeComplexities(String one, String two) {
        if (one == null || one.length() == 0)
            return two;
        if (two == null || two.length() == 0)
            return one;
        return one + "+" + two;
    }

    @Override
    protected int getParallelUnits() {
        return this.input.size();
    }

    @Override
    protected int getBalanceSplitPoint() {
        int s = OpenCLDecider.getSplitPoint(getParallelUnits(), input.size(),
                1,
                filterFun.getSource() + reduceFun.getSource(),
                mergeComplexities(filterFun.getSourceComplexity(),
                        reduceFun.getSourceComplexity()),
                getFeatures()
        );
        if (s < GPUReduce.DEFAULT_MAX_REDUCTION_SIZE) return 0;
        return s;
    }

    @Override
    public void cpuExecution(int start, int end) {
        cpuOp.setLimits(start, end);
        cpuOp.execute();
    }

    @Override
    public void gpuExecution(int start, int end) {
        gpuOp.setLimits(start, end);
        gpuOp.execute();
    }

    @Override
    protected void mergeResults(boolean hasGPU, boolean hasCPU) {
        if (!hasGPU) {
            cpuOp.waitForExecution();
            output = cpuOp.getOutput();
        } else if (!hasCPU) {
            gpuOp.waitForExecution();
            output = gpuOp.getOutput();
        } else {
            gpuOp.waitForExecution();
            cpuOp.waitForExecution();
            output = gpuOp.getOutput();
            output = reduceFun.combine(output, cpuOp.getOutput());
        }
    }

    public T getOutput() {
        // No need for lazyness in reduces.
        execute();
        return output;
    }

    // Utils

    public String getInputType() {
        return input.getContainingType().getSimpleName();
    }

    public String getOutputType() {
        return input.getContainingType().getSimpleName();
    }

    public int getOutputSize() {
        return input.size();
    }

    // Getters and Setters

    public void setOutput(PList<T> output) {
        this.output = output.get(0);
    }

    public void setOutput(T output) {
        this.output = output;
    }

    public LambdaReducerWithSeed<T> getReduceFun() {
        return reduceFun;
    }

    public void setReduceFun(LambdaReducerWithSeed<T> reduceFun) {
        this.reduceFun = reduceFun;
    }

    public LambdaFilter<T> getFilterFun() {
        return filterFun;
    }

    public void setFilterFun(LambdaFilter<T> filterFun) {
        this.filterFun = filterFun;
    }

    public String getFeatures() {
        String sum = FeatureHelper.sumFeatures(filterFun.getFeatures(), reduceFun.getFeatures());
        return FeatureHelper.getFullFeatures(sum, input.size(), getInputType(), 1, getOutputType(), 3);
    }
}
