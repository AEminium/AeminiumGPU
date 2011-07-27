package aeminium.gpu.executables;

import aeminium.gpu.devices.GPUDevice;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public abstract class GenericProgram implements Program {
	protected GPUDevice device;
	protected CLProgram program;
	protected CLKernel kernel;
	protected CLEvent kernelCompletion;
	protected ProgramLogger logger = null;

	protected String otherSources;
	protected long startTime;
	
	
	protected abstract boolean willRunOnGPU();
	public abstract void cpuExecution();
	public void gpuExecution() {
		device.execute(this);
	}
	
	
	public void execute() {
		/* Do we have a GPU available?*/
		if (device == null) {
			if (System.getenv("DEBUG") != null) {
				System.out.println("No GPU device available.");
			}
			cpuExecution();
			return;
		}
		
		if (System.getenv("BENCH") != null) {
			boolean isGpu = willRunOnGPU();
			long startT = System.nanoTime();
			gpuExecution();
			long gpuT = System.nanoTime() - startT;
			
			startT = System.nanoTime();
			cpuExecution();
			long cpuT = System.nanoTime() - startT;
			System.out.println("GPUreal: " + gpuT);
			System.out.println("CPUreal: " + cpuT);
			
			if ( isGpu == (gpuT < cpuT) ) {
				System.out.println("GPUvsCPU: right");
			} else {
				System.out.println("GPUvsCPU: wrong");
			}
			return;
		}
		
		/* Regular decision */
		if (willRunOnGPU()) {
			gpuExecution();
		} else {
			cpuExecution();
		}
	}
	
	
	
	// Pipeline
	
	public void prepareSource(CLContext ctx) {
		kernel = getOrCreateKernel(ctx);
	}
	abstract public void prepareBuffers(CLContext ctx);
	abstract public void execute(CLContext ctx, CLQueue q);
	abstract public void retrieveResults(CLContext ctx, CLQueue q);
	
	@Override
	public void waitExecution(CLContext context, CLQueue queue) {
		kernelCompletion.waitFor();
		kernelCompletion = null;
	}
	
	public void release() {
		if (program != null) {
			program.release();
		}
		if (kernelCompletion != null) {
			kernelCompletion.release();
		}
		if (kernel != null) {
			kernel.release();
		}
		System.gc();
	}
	
	// CL Definitions
	
	abstract protected String getSource();
	abstract public String getKernelName();
	
	// Pipeline Helpers
	
	protected CLKernel getOrCreateKernel(CLContext ctx) {
		program = getProgram(ctx);
		return createKernel(program);
	}
	
	protected CLKernel createKernel(CLProgram program) {
		try {
			return program.createKernel(getKernelName());
		} catch (CLBuildException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	protected CLProgram getProgram(CLContext ctx) {
		try {
			if (System.getenv("OPENCL") != null) {
				System.out.println("Compiling Source");
				System.out.println(getSource());
			}
			return ctx.createProgram(getSource()).build();
		} catch (CLBuildException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	
	// Getters/Setters
	
	public void setDevice(GPUDevice dev) {
		device = dev;
	}
	
	public GPUDevice getDevice() {
		return device;
	}
	
	public void setOtherSources(String otherSources) {
		this.otherSources = otherSources;
	}

	public String getOtherSources() {
		return otherSources;
	}
	
	public ProgramLogger getLogger() {
		return logger;
	}

	public void setLogger(ProgramLogger logger) {
		this.logger = logger;
	}

}
