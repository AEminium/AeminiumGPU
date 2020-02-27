package aeminium.gpu.collections.lists;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

import java.util.Arrays;

public class LongList extends AbstractList<Long> implements ConcreteCollection {

	public long[] box;

	public LongList() {
		this(new long[DEFAULT_SIZE], 0);
	}

	public LongList(long[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}

	@Override
	public void add(Long e) {
		ensureOneMore();
		box[size++] = e.longValue();
	}

	@Override
	public void add(int index, Long e) {
		System.arraycopy(box, index, box, index + 1, size - index);
		size++;
		box[index] = e.longValue();
	}

	@Override
	public void remove(Long o) {
		for (int i = 0; i < size; i++) {
			if (box[i] == o.longValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public void clear() {
		size = 0;
		box = new long[DEFAULT_SIZE];
	}

	@Override
	public Long get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Long e) {
		if (index >= size) {
			size = index + 1;
		}
		if (index >= box.length) {
			ensureNMore(index + 1 - box.length);
		}
		box[index] = e;
	}

	@Override
	public Long remove(int index) {
		long e = box[index];
		System.arraycopy(box, index + 1, box, index, size - index);
		size--;
		return e;
	}

	@Override
	public PList<Long> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		long[] newList = new long[Math.max(DEFAULT_SIZE, newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new LongList(newList, newSize);
	}

	@Override
	public Class<?> getContainingType() {
		return Long.class;
	}

	// Utilities

	public long[] getArray() {
		return box;
	}

	protected void ensureNMore(int n) {
        if (size + n > box.length) {
            int plus = Math.max(n, INCREMENT_SIZE);
			box = Arrays.copyOf(box, box.length + plus);
		}
	}

	protected void ensureOneMore() {
		if (size == box.length) {
			box = Arrays.copyOf(box, box.length + INCREMENT_SIZE);
		}
	}

	@Override
	public PList<Long> extend(PList<Long> extra) {
		if (extra instanceof LongList) {
			LongList other = (LongList) extra;
			ensureNMore(extra.size());
			System.arraycopy(other.box, 0, box, size, other.size());
			size += other.size;
		} else {
			for (int i=0; i<extra.size(); i++) this.add(extra.get(i));
		}
		return this;
	}
	
	@Override
	public PList<Long> extendAt(int i, PList<Long> extra) {
		if (extra instanceof LongList) {
			LongList other = (LongList) extra;
			ensureNMore((other.size() + i) - this.size());
			System.arraycopy(other.box, 0, box, i, extra.size());
			if (i + other.size > size) size = i + other.size;
		} else {
			for (Long b : extra) {
				this.set(i, b);
			}
		}
		return null;
	}
	
	@Override
	public void replaceBy(PList<?> newList) {
		box = ((LongList) newList).box;
		size = newList.size();
	}
	
	@Override
	public PObject copy() {
		return new LongList(box, size);
	}
}
