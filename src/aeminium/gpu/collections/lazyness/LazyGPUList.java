package aeminium.gpu.collections.lazyness;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.AbstractList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;

public class LazyGPUList<T> extends AbstractList<T> implements PList<T> {

	private PList<T> cpuList = null;
	private String type;
	
	private CLBuffer<?> buffer;

	private CLEvent event;
	private GPUDevice device;
	

	public LazyGPUList(CLBuffer<?> buffer, String type, int size, GPUDevice dev, CLEvent e) {
		super();
		this.size = size;
		this.buffer = buffer;
		this.type = type;
		this.event = e;
		this.device = dev;
	}

	public boolean hasBeenEvaluated() {
		return cpuList != null;
	}
	
	@SuppressWarnings("unchecked")
	public PList<T> evaluate() {
		if (cpuList == null) {
			cpuList = (PList<T>) BufferHelper.extractFromBuffer(buffer, device.getQueue(), event, type, size);
		}
		return cpuList;
	}

	@Override
	public int size() {
		return (cpuList != null ? cpuList.size() : size);
	}

	@Override
	public void add(int index, T e) {
		evaluate();
		cpuList.add(index, e);
	}

	@Override
	public void remove(T o) {
		evaluate();
		cpuList.remove(o);
	}

	@Override
	public T get(int index) {
		evaluate();
		return cpuList.get(index);
	}

	@Override
	public void set(int index, T e) {
		evaluate();
		cpuList.set(index, e);
	}

	@Override
	public T remove(int index) {
		evaluate();
		return cpuList.remove(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		size = 0;
		cpuList = (PList<T>) CollectionFactory.listFromType(getType()
				.getSimpleName().toString());
	}

	@Override
	public PList<T> subList(int fromIndex, int toIndex) {
		evaluate();
		return cpuList.subList(fromIndex, toIndex);
	}

	@Override
	public Class<?> getType() {
		return cpuList.getType();
	}

}
