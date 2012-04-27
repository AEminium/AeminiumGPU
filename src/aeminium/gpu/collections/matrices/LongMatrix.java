package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.LongList;
import aeminium.gpu.collections.lists.PList;

public class LongMatrix extends AbstractMatrix<Long> {

	protected long[] box;
	
	public LongMatrix(int m, int n) {
		this(new long[m*n], m, n);
	}
	
	public LongMatrix(long[] box, int m, int n) {
		super(m,n);
		this.box = box;
	}

	@Override
	public Long get(int i, int j) {
		return box[i * cols + j];
	}

	@Override
	public void set(int i, int j, Long e) {
		box[i * cols + j] = e;
	}

	@Override
	public Class<?> getType() {
		return Long.class;
	}
	
	@Override
	public PList<Long> elements() {
		return new LongList(box,size);
	}
		
}
