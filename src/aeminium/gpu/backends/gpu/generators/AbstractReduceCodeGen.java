package aeminium.gpu.backends.gpu.generators;

public abstract class AbstractReduceCodeGen extends AbstractCodeGen{
	public abstract String getMapLambdaSource();
	public abstract String getReduceLambdaSource();
	public abstract String getReduceKernelSource();
	public abstract String getReduceKernelName();
	public abstract String getMapLambdaName();
	protected boolean hasSeed = false;
	
	public void setHasSeed(boolean b) {
		hasSeed = b;
	}
	
}
