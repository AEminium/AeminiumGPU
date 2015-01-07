package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.AbstractPObject;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.operations.ReducerWithSeed;
import aeminium.gpu.operations.RecursiveOp;

public abstract class RecursiveStrategy<R,T> extends AbstractPObject implements ReducerWithSeed<T> {

	public abstract T iterative(R r, R l, RecursiveCallback result);
	
	public abstract R getStart();
	
	public abstract R getEnd();
	
	public abstract void split(PList<R> indices, int index, R s, R e, int n);
	
	public T evaluate() {
		RecursiveOp<R,T> op = new RecursiveOp<R,T>(this);
		return op.getOutput();
	}
	
}
