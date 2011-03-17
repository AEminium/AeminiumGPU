package aeminium.gpu.buffers;

import java.nio.DoubleBuffer;

import aeminium.gpu.lists.DoubleList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class DoubleBufferFactory implements IBufferFactory{

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		double[] ar = ((DoubleList) list).getArray();
		DoubleBuffer ibuffer = DoubleBuffer.wrap(ar);
		return  context.createDoubleBuffer(CLMem.Usage.Input, ibuffer, true);
	}
	
	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createDoubleBuffer(CLMem.Usage.Output, size);
	}
	
	@Override
	public CLBuffer<?> createSharedBufferFor(CLContext context, String type,
			int size) {
		return context.createDoubleBuffer(CLMem.Usage.InputOutput, size);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		double[] content = new double[size];
		outbuffer.asCLDoubleBuffer().read(q, ev).get(content);
		return new DoubleList(content, size);	
	}

}