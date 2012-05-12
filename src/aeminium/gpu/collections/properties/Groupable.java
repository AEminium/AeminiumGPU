package aeminium.gpu.collections.properties;

import aeminium.gpu.collections.matrices.PMatrix;

public interface Groupable<T> {
	public PMatrix<T> groupBy(int n);
}
