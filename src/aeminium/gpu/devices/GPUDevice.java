package aeminium.gpu.devices;

import aeminium.gpu.executables.Program;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public class GPUDevice {
	private CLContext context;
	private CLQueue queue;
	
	public GPUDevice(CLContext ctx) {
		this.context = ctx;
		queue = context.createDefaultQueue();
	}
	
	// Execute Programs
	
	public void compile(String kernel) {
		CLProgram p;
		try {
			p = context.createProgram(kernel).build();
			p.release();
		} catch (CLBuildException e) {
			// GPU not available during compilation. 
		}
		
	}
	
	public void execute(Program p) {
		long startTime;
		startTime = System.nanoTime();
		p.prepareSource(context);
		if (System.getenv("PROFILE") != null) System.out.println("Prepare Source: " + (System.nanoTime() - startTime));

		startTime = System.nanoTime();
		p.prepareBuffers(context);
		if (System.getenv("PROFILE") != null) System.out.println("Prepare Buffers: " + (System.nanoTime() - startTime));
		
		startTime = System.nanoTime();
		p.execute(context, queue);
		if (System.getenv("PROFILE") != null) System.out.println("Execution: " + (System.nanoTime() - startTime));
		
		startTime = System.nanoTime();
		p.retrieveResults(context, queue);
		if (System.getenv("PROFILE") != null) System.out.println("Results Retrieval: " + (System.nanoTime() - startTime));
		
		p.release();
	}
	
	public void release() {
		if (queue != null) {
			queue.finish();
			queue.release();
		}
		if (context != null) {
			context.release();
		}
	}
	
	
	// OpenCL data
	
	public CLDevice getDevice() {
		return context.getDevices()[0];
	}
	
	public CLDevice[] getAllDevices() {
		return context.getDevices();
	}
	
	// Getters/Setters
	
	public CLContext getContext() {
		return context;
	}

	public void setContext(CLContext context) {
		this.context = context;
	}

	public CLQueue getQueue() {
		return queue;
	}

	public void setQueue(CLQueue queue) {
		this.queue = queue;
	}

	
	
}
