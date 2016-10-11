package aeminium.gpu.collections.lists;

import java.util.Arrays;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class FloatList extends AbstractList<Float> implements
		ConcreteCollection {

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
		System.arraycopy(box, index, box, index + 1, size - index);
		size++;
		box[index] = e.floatValue();
	}

	@Override
	public void remove(Float o) {
		for (int i = 0; i < size; i++) {
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
			size = index + 1;
		}
		if (index >= box.length) {
			ensureNMore(index + 1 - box.length);
		}
		box[index] = e;
	}

	@Override
	public Float remove(int index) {
		float e = box[index];
		System.arraycopy(box, index + 1, box, index, size - index);
		size--;
		return e;
	}

	@Override
	public void clear() {
		size = 0;
		box = new float[DEFAULT_SIZE];
	}

	@Override
	public PList<Float> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		float[] newList = new float[Math.max(DEFAULT_SIZE, newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new FloatList(newList, newSize);
	}

	@Override
	public Class<?> getContainingType() {
		return Float.class;
	}

	// Utilities

	public float[] getArray() {
		return box;
	}

	protected synchronized void ensureNMore(int n) {
		if (size >= box.length) {
			int plus = (n > INCREMENT_SIZE) ? n : INCREMENT_SIZE;
			box = Arrays.copyOf(box, box.length + plus);
		}
	}

	protected synchronized void ensureOneMore() {
		if (size == box.length) {
			box = Arrays.copyOf(box, box.length + INCREMENT_SIZE);
		}
	}
	
	@Override
	public PList<Float> extend(PList<Float> extra) {
		if (extra instanceof FloatList) {
			FloatList other = (FloatList) extra;
			ensureNMore(other.size());
			System.arraycopy(other.box, 0, box, size, extra.size());
			size += other.size;
		} else {
			for (Float b : extra) {
				this.add(b);
			}
		}
		return null;
	}
	
	@Override
	public PList<Float> extendAt(int i, PList<Float> extra) {
		if (extra instanceof FloatList) {
			FloatList other = (FloatList) extra;
			ensureNMore((other.size() + i) - this.size());
			System.arraycopy(other.box, 0, box, i, extra.size());
			if (i + other.size > size) size = i + other.size;
		} else {
			for (Float b : extra) {
				this.set(i, b);
			}
		}
		return null;
	}

	@Override
	public void replaceBy(PList<?> newList) {
		box = ((FloatList) newList).box;
		size = newList.size();
	}
	
	@Override
	public PObject copy() {
		return new FloatList(box, size);
	}
	
}
