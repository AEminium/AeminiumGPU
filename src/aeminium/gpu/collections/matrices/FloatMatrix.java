package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class FloatMatrix extends AbstractMatrix<Float> implements
		ConcreteCollection {

	protected float[] box;

	public FloatMatrix(int m, int n) {
		this(new float[m * n], m, n);
	}

	public FloatMatrix(float[] box, int m, int n) {
		super(m, n);
		this.box = box;
	}

	@Override
	public Float get(int i, int j) {
		return box[i * cols + j];
	}

	@Override
	public void set(int i, int j, Float e) {
		box[i * cols + j] = e;
	}

	@Override
	public Class<?> getContainingType() {
		return Float.class;
	}

	@Override
	public PList<Float> elements() {
		return new FloatList(box, size);
	}

	public void replaceBy(PMatrix<?> newMatrix) {
		FloatMatrix nb = (FloatMatrix) newMatrix;
		box = nb.box;
	}
	
	@Override
	public PObject copy() {
		return new FloatMatrix(box, cols, rows);
	}
}
