package aeminium.gpu.collections.lists;

import java.util.Arrays;

import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class DoubleList extends AbstractList<Double> implements ConcreteCollection {

	protected double[] box;
	
	public DoubleList() {
		this(new double[DEFAULT_SIZE], 0);
	}
	
	public DoubleList(double[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}
	
	@Override
	public void add(Double e) {
		ensureOneMore();
		box[size++] = e.doubleValue();
	}
	
	@Override
	public void add(int index, Double e) {
		System.arraycopy(box, index, box, index+1, size-index);
		size++;
		box[index] = e.doubleValue();
	}

	@Override
	public void remove(Double o) {
		for(int i=0;i<size;i++) {
			if (box[i] == o.doubleValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public Double get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Double e) {
		if (index >= size) {
			ensureNMore(index + 1 - size);
			size = index + 1;
		}
		box[index] = e;
	}

	@Override
	public Double remove(int index) {
		double e = box[index];
		System.arraycopy(box, index+1, box, index, size-index);
		size--;
		return e;
	}
	
	@Override
	public void clear() {
		size = 0;
		box = new double[DEFAULT_SIZE];
	}
	
	

	@Override
	public PList<Double> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		double[] newList = new double[Math.max(DEFAULT_SIZE,newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new DoubleList(newList, newSize);
	}

	
	@Override
	public Class<?> getType() {
		return Double.class;
	}
	
	
	// Utilities
	
	public double[] getArray() {
		return box;
	}
	
	protected void ensureNMore(int n) {
		if (size >= box.length) {
			int plus = (n > INCREMENT_SIZE) ? n : INCREMENT_SIZE; 
			box = Arrays.copyOf(box, box.length + plus );
		}
	}
	
	protected void ensureOneMore() {
		if (size == box.length) {
			box = Arrays.copyOf(box, box.length + INCREMENT_SIZE );
		}
	}

}
