package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class IntMatrix extends AbstractMatrix<Integer> {

	protected int[] box;
	
	public IntMatrix(int m, int n) {
		this(new int[m*n], m, n);
	}
	
	public IntMatrix(int[] box, int m, int n) {
		super(m,n);
		this.box = box;
	}

	@Override
	public Integer get(int i, int j) {
		return box[i * cols + j];
	}

	@Override
	public void set(int i, int j, Integer e) {
		box[i * cols + j] = e;
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}

	// TODO: Make map general.
	@SuppressWarnings("unchecked")
	@Override
	public <O> PMatrix<O> map(LambdaMapper<Integer, O> mapper) {
		IntList output = (IntList) elements().map(mapper);
		return (PMatrix<O>) new IntMatrix(output.getArray(), rows, cols);
	}

	@Override
	public PList<Integer> elements() {
		return new IntList(box,size);
	}
		
}
