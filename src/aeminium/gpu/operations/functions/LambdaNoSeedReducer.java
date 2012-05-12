package aeminium.gpu.operations.functions;


import aeminium.gpu.collections.properties.operations.NoSeedReducer;
import aeminium.gpu.operations.utils.UniqCounter;
public abstract class LambdaNoSeedReducer<I> implements NoSeedReducer<I>, GPUFunction {
	
	private String id = null;
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}
	
	public String getSourceComplexity() {
		return null;
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String[] getParameters() {
		return new String[] { "reduce_input_first", "reduce_input_second" };
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId(); 
		return id;
	}
}
