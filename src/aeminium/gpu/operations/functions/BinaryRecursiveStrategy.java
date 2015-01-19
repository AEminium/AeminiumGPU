package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.AbstractPObject;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.operations.ReducerWithSeed;
import aeminium.gpu.operations.RecursiveCall;
import aeminium.gpu.utils.UniqCounter;

public abstract class BinaryRecursiveStrategy<R, A> extends AbstractPObject implements ReducerWithSeed<R> {
	
	protected String id = null;
	
	public abstract R call(A n, RecursiveCallback result);
	
	public abstract String getSource();
	public abstract String[] getParameters();
	
	public abstract R combine(R input, R other);
	public abstract R getSeed();
	
	public abstract PList<A> split(A n);
	public abstract String getSplitSource();
	
	public R evaluate() {
		RecursiveCall<R, A> op = new RecursiveCall<R, A>(this);
		return op.getOutput();
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId();
		return id;
	}

	public abstract A getArgument();
}
