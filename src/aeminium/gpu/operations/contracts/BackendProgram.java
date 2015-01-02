package aeminium.gpu.operations.contracts;

public interface BackendProgram {
	public void setLimits(int start, int end);
	public void execute();
	public void waitForExecution();
}
