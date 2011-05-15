package aeminium.gpu.operations.deciders;

import aeminium.gpu.benchmarker.Configuration;

public class OpenCLDecider {
	
	public static boolean useGPU(int size, String code, String complexity, Runnable run) {
		boolean b = decide(size, code, complexity, run);
		if (System.getenv("DEBUG") != null) {
			if (b) {
				System.out.println("GPUchoice");
			} else {
				System.out.println("CPUchoice");
			}
		}
		return b;
	}
	
	public static boolean decide(int size, String code, String complexity, Runnable run) {
		System.out.println("A");
		if (System.getProperties().containsKey("ForceGPU")) return true;
		if (System.getProperties().containsKey("ForceCPU")) return false;
		System.out.println("B");
		try {
			long gpuTime = getGPUEstimation(size, code, complexity);
			long cpuTime = getCPUEstimation(size, run);
			
			if (System.getenv("DEBUG") != null) {
				System.out.println("GPUexp: " + gpuTime);
				System.out.println("CPUexp: " + cpuTime);
			}
			
			return gpuTime < cpuTime;
			
		} catch(Exception e) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("Failed to to consider GPU vs CPU.");
				e.printStackTrace();
			}
			return true;
		}
	}
	
	
	private static long getCPUEstimation(int size, Runnable run) {
		long before = System.nanoTime();
		run.run();
		return size * (System.nanoTime() - before);
	}

	private static long getGPUEstimation(int size, String code, String complexity) {
		
		int s = 1 * (int)Math.pow(10, ("" + size).length());
		long pTimeGPU;
		// Buffer times
		System.out.println("S:" + s);
		pTimeGPU = Long.parseLong(Configuration.get(s + ".buffer.to"));
		pTimeGPU += Long.parseLong(Configuration.get(s + ".buffer.from"));
		
		// Compilation and execution times
		long unitComp = Long.parseLong(Configuration.get("unit." + s + ".kernel.compilation"));
		long unitExec = Long.parseLong(Configuration.get("unit." + s + ".kernel.execution"));
		
		if (complexity == null || complexity.length() == 0) {
			System.out.println("Simple!");
			return pTimeGPU;
		} else {
			String[] parts = complexity.split("+");
			for (String part: parts) {
				String[] kv = part.split("*");
				try {
					int times = Integer.parseInt(kv[0]);
					String v = kv[1];
					pTimeGPU += (Long.parseLong(Configuration.get( v + "." + s + ".kernel.compilation")) - unitComp);
					pTimeGPU += times * (Long.parseLong(Configuration.get( v + "." + s + ".kernel.execution")) - unitExec);
					System.out.println("v:" + v);
				} catch (Exception e) {
					System.out.println("Failed to get " + part);
				}
				
			}
		
			return pTimeGPU;
		}
	}
}
