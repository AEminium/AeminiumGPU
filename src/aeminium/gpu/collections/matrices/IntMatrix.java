package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class IntMatrix extends AbstractMatrix<Integer> implements
		ConcreteCollection {

	protected int[] box;

	public IntMatrix(int m, int n) {
		this(new int[m * n], m, n);
	}

	public IntMatrix(int[] box, int m, int n) {
		super(m, n);
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
	public Class<?> getContainingType() {
		return Integer.class;
	}

	@Override
	public PList<Integer> elements() {
		return new IntList(box, size);
	}

}
