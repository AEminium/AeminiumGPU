package aeminium.gpu.operations.deciders;

import java.util.HashMap;

import aeminium.gpu.benchmarker.Configuration;

public class OpenCLDecider {
	
	public static HashMap<String, String> opsToConsider = new HashMap<String,String>();
	
	static {
		opsToConsider.put("sin","sin");
		opsToConsider.put("+","sum");
	}
	
	
	public static boolean useGPU(int size, String code, Runnable run) {
		boolean b = decide(size, code, run);
		if (System.getenv("DEBUG") != null) {
			if (b) {
				System.out.println("GPUchoice");
			} else {
				System.out.println("CPUchoice");
			}
		}
		return b;
	}
	
	public static boolean decide(int size, String code, Runnable run) {
		if (System.getProperties().containsKey("ForceGPU")) return true;
		if (System.getProperties().containsKey("ForceCPU")) return false;
		
		try {
			long gpuTime = getGPUEstimation(size, code);
			long cpuTime = getCPUEstimation(size, run);
			
			if (System.getenv("DEBUG") != null) {
				System.out.println("GPUexp: " + gpuTime);
				System.out.println("CPUexp: " + cpuTime);
			}
			
			return gpuTime < cpuTime;
			
		} catch(Exception e) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("Failto to consider GPU vs CPU.");
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

	private static long getGPUEstimation(int size, String code) {
		int s = 1 * (int)Math.pow(10, ("" + size).length());
		long pTimeGPU;
		// Buffer times
		pTimeGPU = Long.parseLong(Configuration.get(s + ".buffer.to"));
		pTimeGPU += Long.parseLong(Configuration.get(s + ".buffer.from"));
		
		// Compilation and execution times
		long unitComp = Long.parseLong(Configuration.get("unit." + s + ".kernel.compilation"));
		long unitExec = Long.parseLong(Configuration.get("unit." + s + ".kernel.execution"));
		
		for (String k : opsToConsider.keySet()) {
			if (code.contains(k)) {
				pTimeGPU += Long.parseLong(Configuration.get( opsToConsider.get(k) + "." + s + ".kernel.compilation")) - unitComp;
				pTimeGPU += Long.parseLong(Configuration.get( opsToConsider.get(k) + "." + s + ".kernel.execution")) - unitExec;
			}
		}			
		return pTimeGPU;
	}
}
