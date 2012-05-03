package aeminium.gpu.buffers;

import org.bridj.Pointer;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.LocalSize;

public class IntBufferFactory implements IBufferFactory{

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		return context.createIntBuffer(CLMem.Usage.Input, Pointer.pointerToInts(((IntList) list).getArray()), true);
	}
	

	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			PList<T> list) {
		return context.createIntBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(((IntList) list).getArray()), true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		Pointer<Integer> ptr = Pointer.allocateInts(size).order(context.getByteOrder());
		return context.createBuffer(CLMem.Usage.Output, ptr, true);
	}
	
	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new LocalSize(size * 4);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		return new IntList(outbuffer.read(q, ev).getInts(), size);
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		return outbuffer.read(q, ev).getInts(1)[0];
	}


	@Override
	public CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size) {
		return context.createIntBuffer(CLMem.Usage.InputOutput, size);
	}

}
