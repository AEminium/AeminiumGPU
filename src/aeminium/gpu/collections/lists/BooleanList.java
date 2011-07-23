package aeminium.gpu.collections.lists;

import java.util.Arrays;

public class BooleanList extends AbstractList<Boolean> {

	protected int[] box;
	
	
	protected int TRUE = 1;
	protected int FALSE = 0; 
	
	public BooleanList() {
		this(new int[DEFAULT_SIZE], 0);
	}
	
	public BooleanList(int[] box, int size) {
		super();
		this.size = size;
		this.box = box;
	}
	
	public int encode(Boolean b) {
		return (b) ? TRUE : FALSE;
	}
	
	public static Boolean decode(int i) {
		return (i > 0) ? Boolean.TRUE : Boolean.FALSE;
	}
	
	@Override
	public void add(Boolean e) {
		ensureOneMore();
		box[size++] = encode(e);
	}
	
	@Override
	public void add(int index, Boolean e) {
		System.arraycopy(box, index, box, index+1, size-index);
		size++;
		box[index] = encode(e);
	}

	@Override
	public void remove(Boolean o) {
		for(int i=0;i<size;i++) {
			if (box[i] == encode(o)) {
				remove(i--);
			}
		}
	}
	
	@Override
	public void clear() {
		size = 0;
		box = new int[DEFAULT_SIZE];
	}

	@Override
	public Boolean get(int index) {
		return decode(box[index]);
	}

	@Override
	public void set(int index, Boolean e) {
		if (index >= size) {
			ensureNMore(index + 1 - size);
			size = index + 1;
		}
		box[index] = encode(e);
	}

	@Override
	public Boolean remove(int index) {
		int e = box[index];
		System.arraycopy(box, index+1, box, index, size-index);
		size--;
		return decode(e);
	}
	
	

	@Override
	public PList<Boolean> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		int[] newList = new int[Math.max(DEFAULT_SIZE,newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new BooleanList(newList, newSize);
	}

	
	@Override
	public Class<?> getType() {
		return Boolean.class;
	}
	
	// Utilities
	
	public int[] getArray() {
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
