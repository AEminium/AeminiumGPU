package aeminium.gpu.operations.functions;


import aeminium.gpu.collections.properties.operations.Reducer;

public abstract class LambdaReducer<I> extends LambdaNoSeedReducer<I> implements Reducer<I>, GPUFunction {
	
	public String getFeatures() {
		return null;
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getSeedSource() {
		return "return " + this.getSeed().toString() + ";";
	}
}
