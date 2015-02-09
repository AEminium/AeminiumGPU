package aeminium.gpu.collections.lazyness;

import java.util.Iterator;
import java.util.Random;

import aeminium.gpu.backends.cpu.MersenneTwisterFast;
import aeminium.gpu.backends.gpu.MersenneTwisterGPU;
import aeminium.gpu.backends.mcpu.MersenneTwister;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.collections.properties.evaluation.LazyCollection;
import aeminium.gpu.collections.properties.evaluation.LazyGPUHelper;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;

public class RandomList implements PList<Float>, LazyCollection {

	public boolean isNative() { return false; }
	
	protected class FloatIdentityMapper extends LambdaMapper<Float, Float> {
		@Override
		public Float map(Float input) {
			return input;
		}

		@Override
		public String getSource() {
			return "return input;";
		}
	}

	protected int max;
	protected int seed;
	protected GPUDevice device;
	protected MersenneTwister mt = null;
	protected MersenneTwisterFast mtf = null;

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
	public Float reduce(LambdaReducerWithSeed<Float> reducer) {
		PList<Float> result = map(new FloatIdentityMapper());
		Reduce<Float> reduceOperation = new Reduce<Float>(reducer, result,
				device);
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
		if (CPUDevice.isMulticore()) {
			if (mt == null)
				mt = new MersenneTwister(seed);
			return mt.nextFloat();
		} else {
			if (mtf == null)
				mtf = new MersenneTwisterFast(seed);
			return mtf.nextFloat();
		}
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
		assert (toIndex >= fromIndex);
		return new RandomList(toIndex - fromIndex);
	}

	@Override
	public Class<?> getContainingType() {
		return Float.class;
	}

	@Override
	public PList<Float> evaluate() {
		return this;
	}

	public int getSeed() {
		return seed;
	}

	@Override
	public PMatrix<Float> groupBy(int cols) {
		return CollectionFactory.matrixfromPList(this, cols);
	}

	@Override
	public LazyGPUHelper getGPUHelper() {
		return new LazyGPUHelper() {

			@Override
			public CLBuffer<?> getInputBuffer(CLContext ctx) {
				MersenneTwisterGPU mt = new MersenneTwisterGPU(device, size(),
						getSeed());
				device.startExecution(mt);
				device.awaitExecution(mt);
				return mt.getOutputBuffer();
			}

		};
	}

	@Override
	public Iterator<Float> iterator() {
		return new Iterator<Float>() {
			
			private int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < size();
			}

			@Override
			public Float next() {
				return get(counter++);
			}
			
		};
	}

	@Override
	public PList<Float> extend(PList<Float> extra) {
		throw new ReadOnlyListException();
	}

	@Override
	public PList<Float> extendAt(int i, PList<Float> extra) {
		throw new ReadOnlyListException();
	}
	
	@Override
	public String getCLType() {
		return "float[]";
	}
}
