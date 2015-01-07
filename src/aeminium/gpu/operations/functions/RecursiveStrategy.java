package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.AbstractPObject;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.operations.ReducerWithSeed;
import aeminium.gpu.operations.RecursiveOp;
import aeminium.gpu.utils.UniqCounter;

public abstract class RecursiveStrategy<R extends Number,T> extends AbstractPObject implements ReducerWithSeed<T> {

	private String id = null;
	
	public abstract T iterative(R r, R l, RecursiveCallback result);
	
	public abstract R getStart();
	
	public abstract R getEnd();
	
	public abstract void split(PList<R> starts, int index, R s, R e, int n);
	
	public T evaluate() {
		RecursiveOp<R,T> op = new RecursiveOp<R,T>(this);
		return op.getOutput();
	}
	
	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSourceComplexity() {
		return null;
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String[] getParameters() {
		return new String[] { "start", "end", "result" };
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId();
		return id;
	}
	
	
}
