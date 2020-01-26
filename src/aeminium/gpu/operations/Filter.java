package aeminium.gpu.operations;

import aeminium.gpu.backends.gpu.GPUFilter;
import aeminium.gpu.backends.mcpu.MCPUFilter;
import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lazyness.LazyPList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.contracts.Program;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.mergers.FilterToFilterMerger;
import aeminium.gpu.operations.mergers.FilterToReduceMerger;
import aeminium.gpu.operations.utils.FeatureHelper;

public class Filter<T> extends GenericProgram implements Program {
    protected PList<T> input;
    protected PList<T> output;
    protected LambdaFilter<T> filterFun;

    protected GPUFilter<T> gpuOp;
    protected MCPUFilter<T> cpuOp;

    public Filter(LambdaFilter<T> filterFun, PList<T> list, GPUDevice dev) {
        this(filterFun, list, "", dev);
    }

    public Filter(LambdaFilter<T> filterFun, PList<T> list, String other, GPUDevice dev) {
        this.device = dev;
        this.input = list;
        this.filterFun = filterFun;

        cpuOp = new MCPUFilter<>(list, filterFun);
        gpuOp = new GPUFilter<>(list, filterFun, other);
        gpuOp.setDevice(dev);
    }

    @Override
    protected int getParallelUnits() {
        return input.size();
    }

    @Override
    protected int getBalanceSplitPoint() {
        return input.size();
//        TODO:
//        return OpenCLDecider.getSplitPoint(input.size(), input.size(), input.size(),
//                filterFun.getSource(), filterFun.getSourceComplexity(), getFeatures());
    }

    private String getFeatures() {
        return FeatureHelper.getFullFeatures(filterFun.getFeatures(), input.size(),
                input.getContainingType().toString(), input.size(), output.getContainingType().toString(), 4);
    }

    @Override
    public void cpuExecution(int start, int end) {
        cpuOp.setLimits(start, end);
        cpuOp.execute();
    }

    @Override
    public void gpuExecution(int start, int end) {
        this.gpuOp.setLimits(start, end);
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
            output.extend(cpuOp.getOutput());
        }
    }

    public PList<T> getOutput() {
        final Filter<T> innerFilter = this;

        // Lazy return
        LazyEvaluator<T> operation = new LazyEvaluator<T>() {
            @Override
            public Object evaluate() {
                innerFilter.execute();
                return output;
            }

            @Override
            public Class<?> getType() {
                return input.getContainingType();
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
                FilterToReduceMerger<T> merger = new FilterToReduceMerger<T>(innerFilter, reduceOp, input);
                return merger.getOutput();
            }

            @Override
            public boolean canMergeWithFilter(LambdaFilter<T> filterFun) {
                return true;
            }

            @Override
            public PList<T> mergeWithFilter(Filter<T> filterOp) {
                FilterToFilterMerger<T> merger = new FilterToFilterMerger<T>(innerFilter, filterOp, input);
                return merger.getOutput();
            }
        };
//        TODO: The size will change after filter, need to check this:
        return new LazyPList<>(operation, input.size());
    }

    public int getOutputSize() {
//        TODO:
        return input.size();
    }

//    Getters and Setters

    public void setOutput(PList<T> output) {
        this.output = output;
    }

    public void setFilterFun(LambdaFilter<T> filterFun) {
        this.filterFun = filterFun;
    }

    public LambdaFilter<T> getFilterFun() {
        return filterFun;
    }

    public GPUFilter<T> getGpuFilter() {
        return gpuOp;
    }
}
