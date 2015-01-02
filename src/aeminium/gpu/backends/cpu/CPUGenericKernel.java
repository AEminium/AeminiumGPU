package aeminium.gpu.backends.cpu;

public abstract class CPUGenericKernel implements CPUKernel {

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
