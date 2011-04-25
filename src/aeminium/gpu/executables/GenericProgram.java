package aeminium.gpu.executables;

import aeminium.gpu.devices.DefaultDeviceFactory;
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
	
	public void run() {
		if (device != null) {
			device = new DefaultDeviceFactory().getDevice();
		}
		device.execute(this);
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
			if (System.getenv("DEBUG") != null) {
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
