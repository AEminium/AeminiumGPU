package aeminium.gpu.devices;

import com.nativelibs4java.opencl.JavaCL;

public class BestContextDeviceFactory implements DeviceFactory {

	@Override
	public GPUDevice getDevice() {
		return new GPUDevice(JavaCL.createBestContext());
	}

}
