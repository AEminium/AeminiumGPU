package aeminium.gpu.lists.lazyness;

import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public class Range implements PList<Integer> {
	
	public class IdentityMapper extends LambdaMapper<Integer, Integer> {

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
	public Integer reduce(LambdaReducer<Integer> reducer) {
		PList<Integer> result = map(new IdentityMapper());
		Reduce<Integer> reduceOperation = new Reduce<Integer>(reducer, result, device);
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
	public PList<Integer> subList(int fromIndex, int toIndex) {
		throw new ReadOnlyListException();
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}

	@Override
	public PList<Integer> evaluate() {
		return this;
	}

}
