package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.AbstractPObject;
import aeminium.gpu.collections.properties.operations.ReducerWithSeed;
import aeminium.gpu.operations.RangedRecursiveCall;
import aeminium.gpu.utils.UniqCounter;

public abstract class Recursive2DStrategy<R extends Number, R2, T> extends AbstractPObject implements ReducerWithSeed<T> {
	protected String id = null;

	public abstract T iterative(R r, R l, R2 t, R2 b, RecursiveCallback result);
	public abstract Range2D<R, R2> split(R s, R e, R2 t, R2 b,int n);
	
	public abstract R getStart();
	public abstract R getEnd();
	public abstract R2 getTop();
	public abstract R2 getBottom();
	
	public T evaluate() {
		RangedRecursiveCall<R, R2, T> op = new RangedRecursiveCall<R, R2, T>(this);
		return op.getOutput();
	}	
	
	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}
	
	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSplitSource() {
		return null;
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSourceComplexity() {
		return null;
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String[] getParameters() {
		return new String[] { "start", "end", "top", "bottom", "result" };
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId();
		return id;
	}
}
