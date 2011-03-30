package aeminium.gpu.lists;

import aeminium.gpu.lists.properties.Mappable;
import aeminium.gpu.lists.properties.Reductionable;

public interface PList<T> extends Mappable<T>, Reductionable<T> {
	
	/* Properties */
	public int size();
	public int length();
	
	public boolean isEmpty();
	
	/* List Management Methods */
	public void add(T e);
	public void add(int index, T e);
	
	public void remove(T o);
	public T remove(int index);
	
	public T get(int index);
	public void set(int index, T e);
	
	
	/* Extraction Methods */
	public PList<T> subList(int fromIndex, int toIndex);
	
	/* Type Methods */
	public Class<?> getType();
	
	/* Lazyness */
	public PList<T> evaluate();

}
