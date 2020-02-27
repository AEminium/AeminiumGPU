package aeminium.gpu.backends.gpu.generators;

public abstract class AbstractMapReduceCodeGen extends AbstractReduceCodeGen {
    public abstract String getMapLambdaSource();

    public abstract String getMapLambdaName();
}
