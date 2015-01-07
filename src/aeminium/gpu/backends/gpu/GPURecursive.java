package aeminium.gpu.backends.gpu;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLQueue;

import aeminium.gpu.operations.functions.RecursiveStrategy;

public class GPURecursive<R, T> extends GPUGenericKernel {

	public T output;
	public RecursiveStrategy<R, T> strategy;
	
	public GPURecursive(RecursiveStrategy<R, T> recursiveStrategy) {
		strategy = recursiveStrategy;
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKernelName() {
		// TODO Auto-generated method stub
		return null;
	}

	public T getOutput() {
		return output;
	}
	
}
