package aeminium.gpu.backends.mcpu;

public abstract class MCPUGenericKernel implements MCPUKernel {

	protected int start;
	protected int end;
	protected int size;
	
	@Override
	public void setLimits(int start, int end) {
		this.start = start;
		this.end = end;
		this.size = end - size;
	}
	
}
