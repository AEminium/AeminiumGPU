package aeminium.gpu.collections.lists;

import aeminium.gpu.collections.PCollection;
import aeminium.gpu.collections.properties.Mappable;
import aeminium.gpu.collections.properties.Reductionable;

public interface PList<T> extends PCollection<T>, Mappable<T>, Reductionable<T> {
	
	/* Properties */
	public int length();
	
	public boolean isEmpty();
	
	/* List Management Methods */
	public void add(T e);
	public void add(int index, T e);
	
	public void remove(T o);
	public T remove(int index);
	
	public T get(int index);
	public void set(int index, T e);
	
	public void clear();
	
	/* Extraction Methods */
	public PList<T> subList(int fromIndex, int toIndex);
	
	/* Lazyness */
	public PList<T> evaluate();

}
