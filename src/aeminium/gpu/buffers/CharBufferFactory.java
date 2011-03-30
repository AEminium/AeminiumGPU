package aeminium.gpu.buffers;

import java.nio.CharBuffer;

import aeminium.gpu.lists.CharList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class CharBufferFactory implements IBufferFactory {

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		char[] ar = ((CharList) list).getArray();
		CharBuffer ibuffer = CharBuffer.wrap(ar, 0, list.size());
		return context.createCharBuffer(CLMem.Usage.Input, ibuffer, true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createCharBuffer(CLMem.Usage.Output, size);
	}

	@Override
	public CLBuffer<?> createSharedBufferFor(CLContext context, String type,
			int size) {
		return context.createCharBuffer(CLMem.Usage.InputOutput, size);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev, int size) {
		char[] content = new char[size];
		outbuffer.asCLCharBuffer().read(q, ev).get(content);
		return new CharList(content, size);
	}

}
