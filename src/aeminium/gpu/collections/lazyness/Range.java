package aeminium.gpu.collections.lazyness;

import java.util.Iterator;

import org.bridj.Pointer;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.collections.properties.evaluation.LazyCollection;
import aeminium.gpu.collections.properties.evaluation.LazyGPUHelper;
import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLMem;

public class Range implements PList<Integer>, LazyCollection {

	public boolean isNative() { return false; }
	
	protected class IntegerIdentityMapper extends
			LambdaMapper<Integer, Integer> {
		@Override
		public Integer map(Integer input) {
			return input;
		}

		@Override
		public String getSource() {
			return "return input;";
		}
	}

	private int max;
	protected GPUDevice device;

	public Range(int max) {
		this.max = max;
		device = (new DefaultDeviceFactory()).getDevice();
	}

	@Override
	public <O> PList<O> map(LambdaMapper<Integer, O> mapper) {
		Map<Integer, O> mapOperation = new Map<Integer, O>(mapper, this, device);
		return mapOperation.getOutput();
	}

	@Override
	public Integer reduce(LambdaReducerWithSeed<Integer> reducer) {
		return this.map(new IntegerIdentityMapper()).reduce(reducer);
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
	public void add(Integer e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void add(int index, Integer e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void remove(Integer o) {
		throw new ReadOnlyListException();
	}

	@Override
	public Integer remove(int index) {
		throw new ReadOnlyListException();
	}

	@Override
	public Integer get(int index) {
		return index;
	}

	@Override
	public void set(int index, Integer e) {
		throw new ReadOnlyListException();
	}

	@Override
	public void clear() {
		throw new ReadOnlyListException();
	}
	
	@Override
	public void replaceBy(PList<?> newList) {
		throw new ReadOnlyListException();
	}

	@Override
	public PList<Integer> subList(int fromIndex, int toIndex) {
		throw new ReadOnlyListException();
	}

	@Override
	public Class<?> getContainingType() {
		return Integer.class;
	}

	@Override
	public PList<Integer> evaluate() {
		return this;
	}

	@Override
	public PMatrix<Integer> groupBy(int cols) {
		return CollectionFactory.matrixfromPList(this, cols);
	}

	@Override
	public LazyGPUHelper getGPUHelper() {
		return new LazyGPUHelper() {

			@Override
			public CLBuffer<?> getInputBuffer(CLContext ctx) {
				Pointer<Integer> ptr = Pointer.allocateInts(1);
				return ctx.createBuffer(CLMem.Usage.Input, ptr, false);
			}

		};
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			
			private int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < size();
			}

			@Override
			public Integer next() {
				return counter++;
			}
			
		};
	}

	@Override
	public PList<Integer> extend(PList<Integer> extra) {
		throw new ReadOnlyListException();
	}
	
	@Override
	public PList<Integer> extendAt(int i, PList<Integer> extra) {
		throw new ReadOnlyListException();
	}

	@Override
	public String getCLType() {
		return "int[]";
	}

	@Override
	public PObject copy() {
		return new Range(max);
	}
}
