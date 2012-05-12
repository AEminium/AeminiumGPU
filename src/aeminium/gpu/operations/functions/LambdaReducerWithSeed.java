package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.properties.operations.ReducerWithSeed;

public abstract class LambdaReducerWithSeed<I> extends LambdaReducer<I>
		implements ReducerWithSeed<I>, GPUFunction {

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSeedSource() {
		return "return " + this.getSeed().toString() + ";";
	}
}
