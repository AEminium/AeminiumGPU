package aeminium.gpu.backends.gpu.generators;

public interface GenericReduceCodeGen {
	public String getMapLambdaSource();
	public String getReduceLambdaSource();
	public String getReduceKernelSource();
	public String getReduceKernelName();
	public String getMapLambdaName();
	
	public void setHasSeed(boolean b);
	public boolean isRange();
	public void setRange(boolean isRange);
	
}
