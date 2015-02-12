package aeminium.gpu.collections.lazyness;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.AbstractMatrix;
import aeminium.gpu.collections.matrices.PMatrix;

public class LazyPMatrix<T> extends AbstractMatrix<T> {

	private PList<T> source;

	public LazyPMatrix(PList<T> src, int cols, int rows) {
		super(cols, rows);
		source = src;
	}

	@Override
	public T get(int i, int j) {
		return source.get(i * cols + j);
	}

	@Override
	public void set(int i, int j, T e) {
		source.set(i * cols + j, e);
	}

	@Override
	public PList<T> elements() {
		return source;
	}

	@Override
	public Class<?> getContainingType() {
		return source.getContainingType();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void replaceBy(PMatrix<?> newMatrix) {
		source = (PList<T>) newMatrix.elements();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PObject copy() {
		return new LazyPMatrix<T>((PList<T>) source.copy(), cols, rows);
	}
}
