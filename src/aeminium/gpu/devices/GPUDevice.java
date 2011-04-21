package aeminium.gpu.devices;

import aeminium.gpu.executables.Program;
import aeminium.gpu.executables.ProgramLogger;

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
		if (p.getLogger() !=  null) {
			executeWithLogger(p);
			return;
		}
		p.prepareSource(context);
		p.prepareBuffers(context);
		p.execute(context, queue);
		p.retrieveResults(context, queue);
		p.release();
	}
	
	private void executeWithLogger(Program p) {
		ProgramLogger logger = p.getLogger();		
		long startTime;
		
		startTime = System.nanoTime();
		p.prepareSource(context);
		logger.saveTime("kernel.compilation", System.nanoTime() - startTime);
		
		startTime = System.nanoTime();
		p.prepareBuffers(context);
		logger.saveTime("buffer.to", System.nanoTime() - startTime);
		
		startTime = System.nanoTime();
		p.execute(context, queue);
		logger.saveTime("kernel.execution", System.nanoTime() - startTime);
		
		startTime = System.nanoTime();
		p.retrieveResults(context, queue);
		logger.saveTime("buffer.from", System.nanoTime() - startTime);
		
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
