package aeminium.gpu.backends.gpu.generators;

public abstract class AbstractReduceCodeGen extends AbstractCodeGen {

    public abstract String getReduceLambdaSource();

    public abstract String getReduceKernelSource();

    public abstract String getReduceKernelName();

    protected boolean hasSeed = false;

    public void setHasSeed(boolean b) {
        hasSeed = b;
    }

}
