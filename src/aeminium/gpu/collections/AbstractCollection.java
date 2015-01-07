package aeminium.gpu.collections;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;

public abstract class AbstractCollection {
	protected int size;
	
	protected GPUDevice device;
	
	public int size() {
		return size;
	}
	
	public GPUDevice getDevice() {
		if (device == null) device = (new DefaultDeviceFactory()).getDevice();
		return device;
	}

	public void setDevice(GPUDevice device) {
		this.device = device;
	}
	
	public abstract Class<?> getContainingType();
	public String getCLType() {
		return BufferHelper.getCLTypeOf(getContainingType()) + "*";
	}
}
