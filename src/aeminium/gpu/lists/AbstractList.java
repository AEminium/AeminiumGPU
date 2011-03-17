package aeminium.gpu.lists;

import aeminium.gpu.lists.properties.Mappable;
import aeminium.gpu.lists.properties.Reductionable;

public abstract class AbstractList<T> implements PList<T>, Mappable<T>, Reductionable<T> {

	protected static final int DEFAULT_SIZE = 10000;
	protected static final int INCREMENT_SIZE = 1000;
	
	protected int size;
	
	public int size() {
		return size;
	}
	public int length() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void add(T e) {
		add(size, e);
	}

}
