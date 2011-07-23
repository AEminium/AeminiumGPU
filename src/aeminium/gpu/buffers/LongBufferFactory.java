package aeminium.gpu.buffers;

import org.bridj.Pointer;

import aeminium.gpu.collections.lists.LongList;
import aeminium.gpu.collections.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLKernel.LocalSize;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class LongBufferFactory implements IBufferFactory {

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		return context.createLongBuffer(CLMem.Usage.Input, Pointer.pointerToLongs(((LongList) list).getArray()), true);
	}
	
	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context, PList<T> list) {
		return context.createLongBuffer(CLMem.Usage.InputOutput, Pointer.pointerToLongs(((LongList) list).getArray()), true);
	}


	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		Pointer<Long> ptr = Pointer.allocateLongs(size).order(context.getByteOrder());
		return context.createBuffer(CLMem.Usage.Output, ptr, true);
	}
	
	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new CLKernel.LocalSize(size * 8);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		return new LongList(outbuffer.read(q, ev).getLongs(), size);
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		return outbuffer.read(q, ev).getLongs(1)[0];
	}

	@Override
	public CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size) {
		return context.createLongBuffer(CLMem.Usage.InputOutput, size);
	}
}
