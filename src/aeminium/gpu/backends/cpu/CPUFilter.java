package aeminium.gpu.backends.cpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;

public class CPUFilter<O> extends CPUGenericKernel {

    protected PList<O> input;
    protected PList<O> output;
    protected LambdaFilter<O> filterFun;
    protected String outputType;

    public CPUFilter(PList<O> input, LambdaFilter<O> filterFun) {
        this.input = input;
        this.filterFun = filterFun;
        this.outputType = input.getContainingType().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        output = (PList<O>) CollectionFactory.listFromType(outputType);
        for (int i = start; i < end; i++) {
            final O inputElement = input.get(i);
            if (filterFun.filter(inputElement)) {
                output.add(inputElement);
            }
        }
    }

    @Override
    public void waitForExecution() {

    }

    public PList<O> getInput() {
        return input;
    }

    public void setInput(PList<O> input) {
        this.input = input;
    }

    public PList<O> getOutput() {
        return output;
    }

    public void setOutput(PList<O> output) {
        this.output = output;
    }

    public LambdaFilter<O> getFilterFun() {
        return filterFun;
    }

    public void setFilterFun(LambdaFilter<O> filterFun) {
        this.filterFun = filterFun;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }
}
