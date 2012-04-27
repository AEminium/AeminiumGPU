package aeminium.gpu.collections.matrices;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.PartialReduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public abstract class AbstractMatrix<T>  implements PMatrix<T> {

	protected int cols;
	protected int rows;
	
	protected int size;
	protected GPUDevice device;
	
	public AbstractMatrix(int cols, int rows) {
		device = (new DefaultDeviceFactory()).getDevice();
		this.cols = cols;
		this.rows = rows;
		this.size = cols * rows;
	}
	
	public GPUDevice getDevice() {
		return device;
	}

	public void setDevice(GPUDevice device) {
		this.device = device;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int cols() {
		return cols;
	}

	@Override
	public <O> PMatrix<O> map(LambdaMapper<T, O> mapper) {
		PList<O> o = elements().map(mapper).evaluate();
		return CollectionFactory.matrixfromPList(o, rows, cols);
	}
	
	@Override
	public T reduce(LambdaReducer<T> reducer) {
		return elements().reduce(reducer);
	}
	
	@Override
	public PList<T> reduceLines(LambdaReducer<T> lambdaReducer) {
		PartialReduce<T> reduceOperation = new PartialReduce<T>(lambdaReducer, this, this.rows, device);
		return reduceOperation.getOutput();
	}
	
}
