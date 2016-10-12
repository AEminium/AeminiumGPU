package aeminium.gpu.operations.contracts;

import aeminium.gpu.devices.GPUDevice;

public abstract class GenericProgram implements Program {
	protected GPUDevice device;
	protected long startTime;
	protected int splitPoint = 0;

	
	protected abstract int getParallelUnits();
	protected abstract int getBalanceSplitPoint();
	protected abstract void mergeResults(boolean hasGPU, boolean hasCPU);
	
	public abstract void cpuExecution(int start, int end);
	public abstract void gpuExecution(int start, int end);

	public void execute() {
		
		int units = getParallelUnits();
		int split = 0;
		
		
		/* Do we have a GPU available? */
		if (device == null) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("No GPU device available.");
			}
			cpuExecution(0, units);
			mergeResults(false, true); // No GPU, has CPU
			return;
		}

		split = getBalanceSplitPoint();
		if (System.getenv("BENCH") != null) {
			System.out.println("> Split: " + split);
			boolean isGpu = ( split < (units-split) );
			long startT = System.nanoTime();
			gpuExecution(0, units);
			long gpuT = System.nanoTime() - startT;

			startT = System.nanoTime();
			cpuExecution(0, units);
			long cpuT = System.nanoTime() - startT;
			System.out.println("> GPUreal: " + gpuT);
			System.out.println("> CPUreal: " + cpuT);

			if (isGpu == (gpuT < cpuT)) {
				System.out.println("> GPUvsCPU: right");
			} else {
				System.out.println("> GPUvsCPU: wrong");
			}
			mergeResults(true, true);
			return;
		}

		if (System.getenv("FORCE") != null) {
			if (System.getenv("FORCE").equals("GPU")) {
				split = units;
			} else {
				split = 0;
			}
		}
		boolean hasGPU = split > 0;
		boolean hasCPU = split < units;
		
		if (hasGPU) gpuExecution(0, split);
		if (hasCPU) cpuExecution(split, units);
		mergeResults(hasGPU, hasCPU);
	}

	// Getters/Setters

	public void setDevice(GPUDevice dev) {
		device = dev;
	}

	public GPUDevice getDevice() {
		return device;
	}
}
