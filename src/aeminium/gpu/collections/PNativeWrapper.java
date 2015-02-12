package aeminium.gpu.collections;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;

public class PNativeWrapper<T extends Number> extends AbstractPObject implements PObject {

	T cont;
	
	public PNativeWrapper(T v) {
		cont = v;
	}
	
	@Override
	public String getCLType() {
		return BufferHelper.getCLTypeOf(cont.getClass());
	}

	public boolean isNative() { return true; }
	
	public T getVal() { return cont; }
	
	@Override
	public PObject copy() {
		return new PNativeWrapper<T>(cont);
	}
	
}
