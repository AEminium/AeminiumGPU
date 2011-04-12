package aeminium.gpu.buffers;

import java.nio.DoubleBuffer;

import aeminium.gpu.lists.DoubleList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLKernel.LocalSize;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class DoubleBufferFactory implements IBufferFactory{

	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		double[] ar = ((DoubleList) list).getArray();
		DoubleBuffer ibuffer = DoubleBuffer.wrap(ar, 0, list.size());
		return  context.createDoubleBuffer(CLMem.Usage.Input, ibuffer, true);
	}
	
	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context, PList<T> list) {
		double[] ar = ((DoubleList) list).getArray();
		DoubleBuffer ibuffer = DoubleBuffer.wrap(ar, 0, list.size());
		return  context.createDoubleBuffer(CLMem.Usage.InputOutput, ibuffer, true);
	}
	
	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		return context.createDoubleBuffer(CLMem.Usage.Output, size);
	}
	
	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new CLKernel.LocalSize(size * 8);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size) {
		double[] content = new double[size];
		outbuffer.asCLDoubleBuffer().read(q, ev).get(content);
		return new DoubleList(content, size);	
	}
	
	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev) {
		double[] content = new double[1];
		outbuffer.asCLDoubleBuffer().read(q, ev).get(content);
		return content[1];
	}

}