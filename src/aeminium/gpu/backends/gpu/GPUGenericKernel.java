package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.devices.GPUDevice;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public abstract class GPUGenericKernel implements GPUKernel {
	protected GPUDevice device;
	protected CLProgram program;
	protected CLKernel kernel;
	protected CLEvent kernelCompletion;
	
	protected OtherData[] otherData;
	
	protected String otherSources;

	protected long startTime;
	
	protected int start;
	protected int end;
	protected int size;
	
	@Override
	public void setLimits(int start, int end) {
		this.start = start;
		this.end = end;
		this.size = end - size;
	}
	
	@Override
	public void execute() {
		device.startExecution(this);
	}
	
	@Override
	public void waitForExecution() {
		device.awaitExecution(this);
	}
	
	@Override
	public void prepareSource(CLContext ctx) {
		kernel = getOrCreateKernel(ctx);
	}
	public void prepareBuffers(CLContext ctx) {
		for (OtherData o : otherData) {
			o.createBuffer(ctx);
		}
	}
	
	protected void setExtraDataArgs(int nArgs, CLKernel kernel) {
		if (kernel == null) return;
		int i = nArgs - otherData.length;
		for (OtherData o : otherData) {
			if (o == null) continue;
			kernel.setArg(i++, o.getBuffer());
		}
	}

	abstract public void execute(CLContext ctx, CLQueue q);

	abstract public void retrieveResults(CLContext ctx, CLQueue q);
	
	@Override
	public void waitExecution(CLContext context, CLQueue queue) {
		kernelCompletion.waitFor();
		kernelCompletion = null;
	}
	
	// CL Definitions
	public abstract String getSource();
	public abstract String getKernelName();

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
	
	// Pipeline Helpers
	protected CLKernel getOrCreateKernel(CLContext ctx) {
		return getOrCreateKernel(ctx, getKernelName());
	}

	protected CLKernel getOrCreateKernel(CLContext ctx, String kernelName) {
		program = compileProgram(ctx);
		return getKernel(program, kernelName);
	}
	
	protected CLProgram compileProgram(CLContext ctx) {
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

	protected CLKernel getKernel(CLProgram program, String kernelName) {
		try {
			return program.createKernel(kernelName);
		} catch (CLBuildException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	public String getOtherSources() {
		return otherSources;
	}

	public void setOtherSources(String otherSources) {
		this.otherSources = otherSources;
	}
	
	public void setDevice(GPUDevice dev) {
		this.device = dev;
	}
}
