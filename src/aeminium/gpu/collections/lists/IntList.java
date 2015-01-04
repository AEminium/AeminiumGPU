package aeminium.gpu.collections.lists;

import java.util.Arrays;

import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class IntList extends AbstractList<Integer> implements
		ConcreteCollection {

	protected int[] box;

	public IntList() {
		this(new int[DEFAULT_SIZE], 0);
	}

	public IntList(int[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}

	@Override
	public void add(Integer e) {
		ensureOneMore();
		box[size++] = e.intValue();
	}

	@Override
	public void add(int index, Integer e) {
		System.arraycopy(box, index, box, index + 1, size - index);
		size++;
		box[index] = e.intValue();
	}

	@Override
	public void remove(Integer o) {
		for (int i = 0; i < size; i++) {
			if (box[i] == o.intValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public Integer get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Integer e) {
		if (index >= size) {
			size = index + 1;
		}
		if (index >= box.length) {
			ensureNMore(index + 1 - box.length);
		}
		box[index] = e;
	}

	@Override
	public Integer remove(int index) {
		int e = box[index];
		System.arraycopy(box, index + 1, box, index, size - index);
		size--;
		return e;
	}

	@Override
	public void clear() {
		size = 0;
		box = new int[DEFAULT_SIZE];
	}

	@Override
	public PList<Integer> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		int[] newList = new int[Math.max(DEFAULT_SIZE, newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new IntList(newList, newSize);
	}

	// Utilities

	public int[] getArray() {
		return box;
	}

	protected void ensureNMore(int n) {
		if (size >= box.length) {
			int plus = (n > INCREMENT_SIZE) ? n : INCREMENT_SIZE;
			box = Arrays.copyOf(box, box.length + plus);
		}
	}

	protected void ensureOneMore() {
		if (size == box.length) {
			box = Arrays.copyOf(box, box.length + INCREMENT_SIZE);
		}
	}

	public Class<?> getType() {
		return Integer.class;
	}

	@Override
	public PList<Integer> extend(PList<Integer> extra) {
		if (extra instanceof IntList) {
			IntList other = (IntList) extra;
			ensureNMore(other.size());
			System.arraycopy(other.box, 0, box, size, extra.size());
		} else {
			for (Integer b : extra) {
				this.add(b);
			}
		}
		return null;
	}
	
}
