package aeminium.gpu.collections.lists;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

import java.util.Arrays;

public class CharList extends AbstractList<Character> implements
		ConcreteCollection {

	protected char[] box;

	public CharList() {
		this(new char[DEFAULT_SIZE], 0);
	}

	public CharList(char[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}

	@Override
	public void add(Character e) {
		ensureOneMore();
		box[size++] = e.charValue();
	}

	@Override
	public void add(int index, Character e) {
		System.arraycopy(box, index, box, index + 1, size - index);
		size++;
		box[index] = e.charValue();
	}

	@Override
	public void remove(Character o) {
		for (int i = 0; i < size; i++) {
			if (box[i] == o.charValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public Character get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Character e) {
		if (index >= size) {
			size = index + 1;
		}
		if (index >= box.length) {
			ensureNMore(index + 1 - box.length);
		}
		box[index] = e;
	}

	@Override
	public Character remove(int index) {
		char e = box[index];
		System.arraycopy(box, index + 1, box, index, size - index);
		size--;
		return e;
	}

	@Override
	public void clear() {
		size = 0;
		box = new char[DEFAULT_SIZE];
	}

	@Override
	public PList<Character> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		char[] newList = new char[Math.max(DEFAULT_SIZE, newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new CharList(newList, newSize);
	}

	@Override
	public Class<?> getContainingType() {
		return Character.class;
	}

	@Override
	public String toString() {
		return new String(box, 0, size);
	}

	// Utilities

	public char[] getArray() {
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
	public PList<Character> extend(PList<Character> extra) {
		if (extra instanceof CharList) {
			CharList other = (CharList) extra;
			ensureNMore(other.size());
			System.arraycopy(other.box, 0, box, size, extra.size());
			size += other.size;
		} else {
			for (Character b : extra) {
				this.add(b);
			}
		}
		return null;
	}
	
	@Override
	public PList<Character> extendAt(int i, PList<Character> extra) {
		if (extra instanceof CharList) {
			CharList other = (CharList) extra;
			ensureNMore((other.size() + i) - this.size());
			System.arraycopy(other.box, 0, box, i, extra.size());
			if (i + other.size > size) size = i + other.size;
		} else {
			for (Character b : extra) {
				this.set(i, b);
			}
		}
		return null;
	}
	
	@Override
	public void replaceBy(PList<?> newList) {
		box = ((CharList) newList).box;
		size = newList.size();
	}

	@Override
	public PObject copy() {
		return new CharList(box, size);
	}
	
}
