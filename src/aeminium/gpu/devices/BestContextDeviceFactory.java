package aeminium.gpu.devices;

import com.nativelibs4java.opencl.CLException;
import com.nativelibs4java.opencl.JavaCL;

public class BestContextDeviceFactory implements DeviceFactory {

	@Override
	public GPUDevice getDevice() {
		if (JavaCL.listGPUPoweredPlatforms().length > 0) {
			try {
				return new GPUDevice(JavaCL.createBestContext());
			} catch(CLException e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
