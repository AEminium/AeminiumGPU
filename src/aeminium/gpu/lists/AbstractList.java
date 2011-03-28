package aeminium.gpu.lists;

import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.lists.properties.Mappable;
import aeminium.gpu.lists.properties.Reductionable;

public abstract class AbstractList<T> implements PList<T>, Mappable<T>, Reductionable<T> {

	protected static final int DEFAULT_SIZE = 10000;
	protected static final int INCREMENT_SIZE = 1000;
	
	protected int size;
	protected GPUDevice device;
	
	public AbstractList() {
		device = (new DefaultDeviceFactory()).getDevice();
	}

	public int size() {
		return size;
	}
	public int length() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void add(T e) {
		add(size, e);
	}
	
	public PList<T> evaluate() {
		return this;
	}

	public GPUDevice getDevice() {
		return device;
	}

	public void setDevice(GPUDevice device) {
		this.device = device;
	}
	
}
