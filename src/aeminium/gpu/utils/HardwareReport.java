package aeminium.gpu.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.InfoName;
import com.nativelibs4java.opencl.JavaCL;

public class HardwareReport {
	public static void main(String[] args) {
		for (CLPlatform platform : JavaCL.listPlatforms()) {
			System.out.println("============= " + platform.toString()
					+ " ============");
			getHardwareReportComponent(platform);
		}
	}

	public static void getHardwareReportComponent(CLPlatform platform) {
		List<Map<String, Object>> list = listInfos(platform);
		if (!list.isEmpty()) {
			Set<String> keys = list.get(0).keySet();
			for (Map<String, Object> device : list) {
				Object value = device.get("CL_DEVICE_NAME");
				System.out.println("device: " + value);
				for (String key : keys) {
					value = device.get(key);
					if (value  instanceof Object[]) {
						System.out.print(key + ": ");
						for (Object o : (Object[]) value) {
							System.out.print(o + ", ");
						}
						System.out.println();
					} else {
						System.out.println(key + ": " + value);
					}
				}
			}
		}
	}

	public static Map<String, Method> infoMethods(Class<?> c) {
		Map<String, Method> mets = new TreeMap<String, Method>();
		for (Method met : c.getMethods()) {
			InfoName name = met.getAnnotation(InfoName.class);
			if (name == null) {
				continue;
			}
			mets.put(name.value(), met);
		}
		return mets;
	}

	public static List<Map<String, Object>> listInfos(CLPlatform platform) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		Map<String, Method> platMets = infoMethods(CLPlatform.class);
		Map<String, Method> devMets = infoMethods(CLDevice.class);
		Map<String, Object> platInfos = new TreeMap<String, Object>();
		for (Map.Entry<String, Method> platMet : platMets.entrySet()) {
			try {
				platInfos.put(platMet.getKey(),
						platMet.getValue().invoke(platform));
			} catch (InvocationTargetException ex) {
				if (ex.getCause() instanceof UnsupportedOperationException)
					platInfos.put(platMet.getKey(), "n/a");
				else {
					platInfos.put(platMet.getKey(), "n/a");	
				}
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		for (CLDevice device : platform.listAllDevices(false)) {
			Map<String, Object> devInfos = new TreeMap<String, Object>(
					platInfos);
			for (Map.Entry<String, Method> devMet : devMets.entrySet()) {
				try {
					devInfos.put(devMet.getKey(),
							devMet.getValue().invoke(device));
				} catch (InvocationTargetException ex) {
					if (ex.getCause() instanceof UnsupportedOperationException)
						devInfos.put(devMet.getKey(), "n/a");
					else {
						devInfos.put(devMet.getKey(), "n/a");
					}
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
			ret.add(devInfos);
		}
		return ret;
	}
}
