package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.AbstractCollection;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.PartialReduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public abstract class AbstractMatrix<T> extends AbstractCollection<T> implements PMatrix<T> {

	protected int cols;
	protected int rows;

	public AbstractMatrix(int rows, int cols) {
		this.cols = cols;
		this.rows = rows;
		this.size = cols * rows;
	}
	
	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int cols() {
		return cols;
	}

	@Override
	public <O> PMatrix<O> map(LambdaMapper<T, O> mapper) {
		PList<O> o = elements().map(mapper).evaluate();
		return CollectionFactory.matrixfromPList(o, rows, cols);
	}

	@Override
	public T reduce(LambdaReducerWithSeed<T> reducer) {
		return elements().reduce(reducer);
	}

	@Override
	public PList<T> reduceLines(LambdaReducer<T> lambdaReducer) {
		PartialReduce<T> reduceOperation = new PartialReduce<T>(lambdaReducer,
				this.elements(), this.rows, getDevice());
		return reduceOperation.getOutput();
	}
	
}
