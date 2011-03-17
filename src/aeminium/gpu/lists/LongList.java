package aeminium.gpu.lists;

import java.util.Arrays;

import aeminium.gpu.lists.properties.operations.Mapper;
import aeminium.gpu.lists.properties.operations.Reducer;

public class LongList extends AbstractList<Long> {

	protected long[] box;
	
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
		System.arraycopy(box, index, box, index+1, size-index);
		size++;
		box[index] = e.longValue();
	}

	@Override
	public void remove(Long o) {
		for(int i=0;i<size;i++) {
			if (box[i] == o.longValue()) {
				remove(i--);
			}
		}
	}

	@Override
	public Long get(int index) {
		return box[index];
	}

	@Override
	public void set(int index, Long e) {
		if (index >= size) {
			ensureNMore(index + 1 - size);
			size = index + 1;
		}
		box[index] = e;
	}

	@Override
	public Long remove(int index) {
		long e = box[index];
		System.arraycopy(box, index+1, box, index, size-index);
		size--;
		return e;
	}
	
	

	@Override
	public PList<Long> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		long[] newList = new long[Math.max(DEFAULT_SIZE,newSize)];
		System.arraycopy(box, fromIndex, newList, 0, newSize);
		return new LongList(newList, newSize);
	}

	
	@Override
	public Class<?> getType() {
		return Long.class;
	}
	
	// Data-Parallel Operations
	
	@Override
	public <O> PList<O> map(Mapper<Long, O> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long reduce(Reducer<Long> reducer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// Utilities
	
	public long[] getArray() {
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
