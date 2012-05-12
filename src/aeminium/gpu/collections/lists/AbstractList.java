package aeminium.gpu.collections.lists;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.collections.properties.Mappable;
import aeminium.gpu.collections.properties.Reductionable;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public abstract class AbstractList<T> implements PList<T>, Mappable<T>, Reductionable<T> {

	protected static final int DEFAULT_SIZE = 10000;
	protected static final int INCREMENT_SIZE = 1000;
	
	protected int size;
	protected GPUDevice device;
	
	public AbstractList() {
		device = (new DefaultDeviceFactory()).getDevice();
	}

	public int size() {
		return size;
	}
	public int length() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void add(T e) {
		add(size, e);
	}
	
	public PList<T> evaluate() {
		return this;
	}

	public GPUDevice getDevice() {
		return device;
	}

	public void setDevice(GPUDevice device) {
		this.device = device;
	}
	
	
	@Override
	public <O> PList<O> map(LambdaMapper<T, O> mapper) {
		Map<T, O> mapOperation = new Map<T, O>(mapper, this, device);
		return mapOperation.getOutput();
	}
	
	@Override
	public T reduce(LambdaReducerWithSeed<T> reducer) {
		Reduce<T> reduceOperation = new Reduce<T>(reducer, this, device);
		return reduceOperation.getOutput();
	}
	
	public PMatrix<T> groupBy(int cols) {
		return CollectionFactory.matrixfromPList(this, cols);
	}
	
}
