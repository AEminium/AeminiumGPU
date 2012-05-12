package aeminium.gpu.devices;

import java.util.HashMap;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.CLPlatform.ContextProperties;

public class FirstDeviceFactory implements DeviceFactory {

	public GPUDevice getDevice() {
		for (CLPlatform plat : JavaCL.listPlatforms()) {
			for (CLDevice dev : plat.listCPUDevices(true)) {
				// INFO: Uncomment to know which device is being used.
				// System.out.println("O:" + dev.getName());
				CLContext ctx = plat.createContext(
						new HashMap<ContextProperties, Object>(), dev);
				return new GPUDevice(ctx);
			}
		}
		return null;
	}

}
