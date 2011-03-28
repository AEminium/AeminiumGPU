package aeminium.gpu.operations.functions;

import aeminium.gpu.lists.properties.operations.Mapper;

public abstract class LambdaMapper<I,O> implements Mapper<I,O>, GPUFunction {
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}
}
