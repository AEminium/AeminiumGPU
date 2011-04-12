package aeminium.gpu.buffers;

import java.nio.LongBuffer;

import aeminium.gpu.lists.LongList;
import aeminium.gpu.lists.PList;

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
		long[] ar = ((LongList) list).getArray();
		LongBuffer ibuffer = LongBuffer.wrap(ar, 0, list.size());
		return  context.createLongBuffer(CLMem.Usage.Input, ibuffer, true);
	}
	
	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context, PList<T> list) {
		long[] ar = ((LongList) list).getArray();
		LongBuffer ibuffer = LongBuffer.wrap(ar, 0, list.size());
		return  context.createLongBuffer(CLMem.Usage.InputOutput, ibuffer, true);
	}


	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createLongBuffer(CLMem.Usage.Output, size);
	}
	
	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new CLKernel.LocalSize(size * 8);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		long[] content = new long[size];
		outbuffer.asCLLongBuffer().read(q, ev).get(content);
		return new LongList(content, size);	
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		long[] content = new long[1];
		outbuffer.asCLLongBuffer().read(q, ev).get(content);
		return content[0];
	}
}
