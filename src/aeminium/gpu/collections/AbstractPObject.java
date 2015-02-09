package aeminium.gpu.collections;

import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;

public abstract class AbstractPObject {
	
	protected GPUDevice device;
	
	public boolean isNative() { return false; }
	
	public GPUDevice getDevice() {
		if (device == null) device = (new DefaultDeviceFactory()).getDevice();
		return device;
	}

	public void setDevice(GPUDevice device) {
		this.device = device;
	}
	
	
}
