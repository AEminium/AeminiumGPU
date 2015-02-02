package aeminium.gpu.devices;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLException;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

public class BestContextDeviceFactory implements DeviceFactory {

	static GPUDevice d;

	@Override
	public GPUDevice getDevice() {
		if (d != null) return d;
		for (CLPlatform p : JavaCL.listGPUPoweredPlatforms()) {
			for (CLDevice dev : p.listDevices(CLDevice.Type.GPU, true)) {
				if (System.getenv("DEBUGCL") != null) {
					System.out.println(p);
					System.out.println(dev);
					System.out.println(dev.getVendor());
				}
				try {
					CLContext ctx = JavaCL.createContext(null, dev);
					if (dev.getType().contains(CLDevice.Type.GPU)) {
						d = new GPUDevice(ctx);
						return d;
					}
				} catch (CLException e) {
					continue;
				}
			}
		}
		return null;
	}

}
