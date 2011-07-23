package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.PCollection;
import aeminium.gpu.collections.properties.MappableToMatrix;
import aeminium.gpu.collections.properties.Reductionable;

public interface PMatrix<T> extends PCollection<T>, MappableToMatrix<T>, Reductionable<T> {
	
	/* Properties */
	public int size();
	public int rows();
	public int cols();
	
	public T get(int i, int j);
	public void set(int i, int j, T e);

}
