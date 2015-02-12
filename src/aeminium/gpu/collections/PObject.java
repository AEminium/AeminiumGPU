package aeminium.gpu.collections;

public interface PObject {
	public String getCLType();
	public boolean isNative();
	public PObject copy();
}
