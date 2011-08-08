package aeminium.gpu.operations.deciders;

import aeminium.gpu.recorder.Configuration;

public class OpenCLDecider {

	public static boolean useGPU(int size, String code, String complexity) {		
		boolean b = decide(size, code, complexity);
		if (System.getenv("BENCH") != null) {
			if (b) {
				System.out.println("> GPUchoice");
			} else {
				System.out.println("> CPUchoice");
			}
		}
		return b;
	}

	public static boolean decide(int size, String code, String complexity) {
		return OpenCLDecider.decide(size, code, complexity, false);
	}
	
	public static boolean decide(int size, String code, String complexity, boolean isRange) {
		if (System.getProperties().containsKey("ForceGPU"))
			return true;
		if (System.getProperties().containsKey("ForceCPU"))
			return false;

		if (complexity == null || complexity.length() == 0) {
			// FIXME: best option for base case.
			return size > 10000;
		}

		try {
			long gpuTime = getGPUEstimation(size, code, complexity, isRange);
			long cpuTime = getCPUEstimation(size, code, complexity, isRange);

			if (System.getenv("BENCH") != null) {
				System.out.println("> GPUexp: " + gpuTime);
				System.out.println("> CPUexp: " + cpuTime);
			}
			return gpuTime < cpuTime;

		} catch (Exception e) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("Failed to to consider GPU vs CPU.");
				e.printStackTrace();
			}
			return true;
		}
	}

	public static long getCPUEstimation(int size, String code,
			String complexity, boolean isRange) {
		long pTimeCPU = 0;
		String[] parts = complexity.split("\\+");
		for (String part : parts) {
			String[] kv = part.split("\\*");
			try {
				int times = Integer.parseInt(kv[0]);
				String v = kv[1];
				pTimeCPU += times
						* (getInterpolatedValue("cpu.execution.", size, "." + v));
			} catch (Exception e) {
				System.out.println("Failed to get " + part + ":>" + e);
				e.printStackTrace();
			}

		}
		return pTimeCPU;
	}

	public static long getGPUEstimation(int size, String code,
			String complexity, boolean isRange) {
		long pTimeGPU = 0;
		// Buffer times
		if (!isRange) {
			pTimeGPU += getInterpolatedValue("gpu.buffer.to.", size, "");
		}
		pTimeGPU += getInterpolatedValue("gpu.buffer.from.", size, "");

		String[] parts = complexity.split("\\+");
		for (String part : parts) {
			String[] kv = part.split("\\*");
			try {
				int times = Integer.parseInt(kv[0]);
				String v = kv[1];
				pTimeGPU += (getInterpolatedValue("gpu.kernel.compilation.",
						size, "." + v));
				pTimeGPU += times
						* (getInterpolatedValue("gpu.kernel.execution.", size,
								"." + v));
			} catch (Exception e) {
				if (System.getenv("DEBUG") != null) {
					System.out.println("Failed to get " + part);
				}
				
			}

		}

		return pTimeGPU;
	}

	public static long getInterpolatedValue(String prefix, int size,
			String sufix) {
		
		int sb = (int) Math.pow(10,Math.floor(Math.log10(size)));
		int st = (int) Math.pow(10,Math.ceil(Math.log10(size)));

		float cutPoint = size / ((float) st);
		long bottom = 0;
		long top = 0;
		try {
			bottom = getOrFail(prefix + sb + sufix);
		} catch (Exception e) {
			return 0;
		}
		try {
			top = getOrFail(prefix + st + sufix);
			if (top == 0)
				throw new Exception();
		} catch (Exception e) {
			return bottom;
		}
		return (long) (bottom * cutPoint + top * (1 - cutPoint));
	}

	private static long getOrFail(String key) {
		String val = Configuration.get(key);
		try {
			return Long.parseLong(val);
		} catch (java.lang.NumberFormatException e) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("Failed to load bench value for " + key
						+ ", value: '" + val + "'.");
			}
			return 0;
		}
	}

}
