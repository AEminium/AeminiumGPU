package aeminium.gpu.backends.gpu.generators;

public abstract class AbstractFilterReduceCodeGen extends AbstractReduceCodeGen {
    public abstract String getFilterLambdaSource();

    public abstract String getFilterLambdaName();
}
