package aeminium.gpu.devices;

import com.nativelibs4java.opencl.JavaCL;

public class BestContextDeviceFactory implements DeviceFactory {

	@Override
	public GPUDevice getDevice() {
		if (JavaCL.listGPUPoweredPlatforms().length > 0) {
			return new GPUDevice(JavaCL.createBestContext());
		} else {
			return null;
		}
	}

}
