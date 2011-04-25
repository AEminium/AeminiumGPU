package aeminium.gpu.executables;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLQueue;

public interface Program {
	
	/* GPU Pipeline */
	public void prepareSource(CLContext ctx);
	public void prepareBuffers(CLContext ctx);
	public void execute(CLContext ctx, CLQueue q);
	public void waitExecution(CLContext context, CLQueue queue);
	public void retrieveResults(CLContext ctx, CLQueue q);
	public void release();
	
	public void setLogger(ProgramLogger logger);
	public ProgramLogger getLogger();
}
