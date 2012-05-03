package aeminium.gpu.collections.lazyness;

import java.util.Random;

import aeminium.gpu.collections.lazyness.helpers.IdentityMapper;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.random.MersenneTwisterFast;

public class RandomList implements PList<Float> {

	protected int max;
	protected int seed;
	protected GPUDevice device;
	protected MersenneTwisterFast mt = null;

	public RandomList(int max) {
		this(max, new Random().nextInt());
	}

	public RandomList(int max, int seed) {
		this.max = max;
		this.seed = seed;
		device = (new DefaultDeviceFactory()).getDevice();
	}


	@Override
	public <O> PList<O> map(LambdaMapper<Float, O> mapper) {
		Map<Float, O> mapOperation = new Map<Float, O>(mapper, this, device);
		return mapOperation.getOutput();
	}

	@Override
	public Float reduce(LambdaReducer<Float> reducer) {
		PList<Float> result = map(new IdentityMapper<Float>());
		Reduce<Float> reduceOperation = new Reduce<Float>(reducer, result, device);
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
	public void add(Float e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void add(int index, Float e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void remove(Float o) {
		throw new ReadOnlyListException();
	}

	@Override
	public Float remove(int index) {
		throw new ReadOnlyListException();
	}

	@Override
	public Float get(int index) {
		if (mt == null)
			mt = new MersenneTwisterFast(seed);
		return mt.nextFloat();
	}

	@Override
	public void set(int index, Float e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void clear() {
		throw new ReadOnlyListException();
	}

	@Override
	public PList<Float> subList(int fromIndex, int toIndex) {
		assert(toIndex >= fromIndex);
		return new RandomList(toIndex - fromIndex);
	}

	@Override
	public Class<?> getType() {
		return Float.class;
	}

	@Override
	public PList<Float> evaluate() {
		return this;
	}


	public int getSeed() {
		return seed;
	}

}
