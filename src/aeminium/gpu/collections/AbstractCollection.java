package aeminium.gpu.collections;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;

public abstract class AbstractCollection<T> extends AbstractPObject implements PCollection<T> {
	protected int size;
	
	public int size() {
		return size;
	}
	

	
	public abstract Class<?> getContainingType();
	public String getCLType() {
		return BufferHelper.getCLTypeOf(getContainingType()) + "*";
	}
}
