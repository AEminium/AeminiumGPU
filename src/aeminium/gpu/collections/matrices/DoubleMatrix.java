package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;

public class DoubleMatrix extends AbstractMatrix<Double> implements ConcreteCollection {

	protected double[] box;
	
	public DoubleMatrix(int m, int n) {
		this(new double[m*n], m, n);
	}
	
	public DoubleMatrix(double[] box, int m, int n) {
		super(m,n);
		this.box = box;
	}

	@Override
	public Double get(int i, int j) {
		return box[i * cols + j];
	}

	@Override
	public void set(int i, int j, Double e) {
		box[i * cols + j] = e;
	}

	@Override
	public Class<?> getType() {
		return Double.class;
	}
	
	@Override
	public PList<Double> elements() {
		return new DoubleList(box,size);
	}
		
}
