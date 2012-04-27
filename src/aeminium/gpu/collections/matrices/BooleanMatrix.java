package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.BooleanList;
import aeminium.gpu.collections.lists.PList;

public class BooleanMatrix extends AbstractMatrix<Boolean> {

	protected int[] box;
	
	public BooleanMatrix(int m, int n) {
		this(new int[m*n], m, n);
	}
	
	public BooleanMatrix(int[] box, int m, int n) {
		super(m,n);
		this.box = box;
	}

	@Override
	public Boolean get(int i, int j) {
		return BooleanList.decode(box[i * cols + j]);
	}

	@Override
	public void set(int i, int j, Boolean e) {
		box[i * cols + j] = BooleanList.encode(e);
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}
	
	@Override
	public PList<Boolean> elements() {
		return new BooleanList(box,size);
	}
		
}
