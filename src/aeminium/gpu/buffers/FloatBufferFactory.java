package aeminium.gpu.buffers;

import java.nio.FloatBuffer;

import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class FloatBufferFactory implements IBufferFactory {

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		float[] ar = ((FloatList) list).getArray();
		FloatBuffer ibuffer = FloatBuffer.wrap(ar, 0, list.size());
		return  context.createFloatBuffer(CLMem.Usage.Input, ibuffer, true);
	}
	
	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context, PList<T> list) {
		float[] ar = ((FloatList) list).getArray();
		FloatBuffer ibuffer = FloatBuffer.wrap(ar, 0, list.size());
		return  context.createFloatBuffer(CLMem.Usage.InputOutput, ibuffer, true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createFloatBuffer(CLMem.Usage.Output, size);
	}
	
	@Override
	public CLBuffer<?> createSharedBufferFor(CLContext context, String type,
			int size) {
		return context.createFloatBuffer(CLMem.Usage.InputOutput, size);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		float[] content = new float[size];
		outbuffer.asCLFloatBuffer().read(q, ev).get(content);
		return new FloatList(content, size);	
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		float[] content = new float[1];
		outbuffer.asCLFloatBuffer().read(q, ev).get(content);
		return content[0];
	}
}
