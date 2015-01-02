package aeminium.gpu.backends.gpu;

import aeminium.gpu.operations.contracts.BackendProgram;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLQueue;

public interface GPUKernel extends BackendProgram {
	
	public void prepareSource(CLContext ctx);
	public void prepareBuffers(CLContext ctx);
	public void execute(CLContext ctx, CLQueue q);
	public void waitExecution(CLContext context, CLQueue queue);
	public void retrieveResults(CLContext ctx, CLQueue q);
	public void release();
}
