package aeminium.gpu.lists;

import java.util.Arrays;

import aeminium.gpu.lists.properties.operations.Mapper;
import aeminium.gpu.lists.properties.operations.Reducer;

public class FloatList extends AbstractList<Float> {

	protected float[] box;
	
	public FloatList() {
		this(new float[DEFAULT_SIZE], 0);
	}
	
	public FloatList(float[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}
	
	@Override
	public void add(Float e) {
		ensureOneMore();
		box[size++] = e.floatValue();
	}
	
	@Override
	public void add(int index, Float e) {
		System.arraycopy(box, index, box, index+1, size-index);
		size++;
		box[index] = e.floatValue();
	}

	@Override
	public void remove(Float o) {
		for(int i=0;i<size;i++) {
			if (box[i] == o.floatValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public Float get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Float e) {
		if (index >= size) {
			ensureNMore(index + 1 - size);
			size = index + 1;
		}
		box[index] = e;
	}

	@Override
	public Float remove(int index) {
		float e = box[index];
		System.arraycopy(box, index+1, box, index, size-index);
		size--;
		return e;
	}
	
	

	@Override
	public PList<Float> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		float[] newList = new float[Math.max(DEFAULT_SIZE,newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new FloatList(newList, newSize);
	}

	
	@Override
	public Class<?> getType() {
		return Float.class;
	}
	
	// Data-Parallel Operations
	
	@Override
	public <O> PList<O> map(Mapper<Float, O> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float reduce(Reducer<Float> reducer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// Utilities
	
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
