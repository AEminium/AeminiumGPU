package aeminium.gpu.devices;

import com.nativelibs4java.opencl.CLException;
import com.nativelibs4java.opencl.JavaCL;

public class BestContextDeviceFactory implements DeviceFactory {

	
	static GPUDevice d;
	
	@Override
	public GPUDevice getDevice() {
		if (d != null) return d;
		if (JavaCL.listGPUPoweredPlatforms().length > 0) {
			try {
				d = new GPUDevice(JavaCL.createBestContext());
				if (System.getenv("DEBUG") != null) System.out.println(JavaCL.createBestContext());
				return d;
			} catch (CLException e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
