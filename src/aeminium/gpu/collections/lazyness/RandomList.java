package aeminium.gpu.collections.lazyness;

import aeminium.gpu.collections.lazyness.helpers.IdentityMapper;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public class RandomList implements PList<Double> {

	private int max;
	protected GPUDevice device;

	public RandomList(int max) {
		this.max = max;
		device = (new DefaultDeviceFactory()).getDevice();
	}


	@Override
	public <O> PList<O> map(LambdaMapper<Double, O> mapper) {
		Map<Double, O> mapOperation = new Map<Double, O>(mapper, this, device);
		return mapOperation.getOutput();
	}

	@Override
	public Double reduce(LambdaReducer<Double> reducer) {
		PList<Double> result = map(new IdentityMapper<Double>());
		Reduce<Double> reduceOperation = new Reduce<Double>(reducer, result, device);
		return reduceOperation.getOutput();
	}


	@Override
	public int size() {
		return max;
	}

	@Override
	public int length() {
		return max;
	}

	@Override
	public boolean isEmpty() {
		return (max > 0);
	}

	@Override
	public void add(Double e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void add(int index, Double e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void remove(Double o) {
		throw new ReadOnlyListException();
	}

	@Override
	public Double remove(int index) {
		throw new ReadOnlyListException();
	}

	@Override
	public Double get(int index) {
		return Math.random();
	}

	@Override
	public void set(int index, Double e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void clear() {
		throw new ReadOnlyListException();
	}

	@Override
	public PList<Double> subList(int fromIndex, int toIndex) {
		assert(toIndex >= fromIndex);
		return new RandomList(toIndex - fromIndex);
	}

	@Override
	public Class<?> getType() {
		return Double.class;
	}

	@Override
	public PList<Double> evaluate() {
		return this;
	}


	public int getSeed() {
		return 777;
	}

}
