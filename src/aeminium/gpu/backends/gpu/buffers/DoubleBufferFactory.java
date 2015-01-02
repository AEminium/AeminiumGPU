package aeminium.gpu.backends.gpu.buffers;

import org.bridj.Pointer;

import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.LocalSize;

public class DoubleBufferFactory implements IBufferFactory {

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		return context.createDoubleBuffer(CLMem.Usage.Input,
				Pointer.pointerToDoubles(((DoubleList) list).getArray()), true);
	}

	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			PList<T> list) {
		return context.createDoubleBuffer(CLMem.Usage.InputOutput,
				Pointer.pointerToDoubles(((DoubleList) list).getArray()), true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		Pointer<Double> ptr = Pointer.allocateDoubles(size).order(
				context.getByteOrder());
		return context.createBuffer(CLMem.Usage.Output, ptr, true);
	}

	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new LocalSize(size * 8);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev, int size) {
		return new DoubleList(outbuffer.read(q, ev).getDoubles(), size);
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		return outbuffer.read(q, ev).getDoubles(1)[0];
	}

	@Override
	public CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size) {
		return context.createDoubleBuffer(CLMem.Usage.InputOutput, size);
	}

}