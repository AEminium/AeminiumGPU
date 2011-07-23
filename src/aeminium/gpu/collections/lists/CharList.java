package aeminium.gpu.collections.lists;

import java.util.Arrays;

public class CharList extends AbstractList<Character> {

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
		System.arraycopy(box, index, box, index+1, size-index);
		size++;
		box[index] = e.charValue();
	}

	@Override
	public void remove(Character o) {
		for(int i=0;i<size;i++) {
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
			ensureNMore(index + 1 - size);
			size = index + 1;
		}
		box[index] = e;
	}

	@Override
	public Character remove(int index) {
		char e = box[index];
		System.arraycopy(box, index+1, box, index, size-index);
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
		char[] newList = new char[Math.max(DEFAULT_SIZE,newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new CharList(newList, newSize);
	}

	
	@Override
	public Class<?> getType() {
		return Character.class;
	}
	
	@Override
	public String toString() {
		return new String(box,0,size);
	}
	
	// Utilities
	
	public char[] getArray() {
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
