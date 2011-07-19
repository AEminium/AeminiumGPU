package aeminium.gpu.buffers;

import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLQueue;

public interface IBufferFactory {
	
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context, PList<T> list);
	
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list);

	public CLBuffer<?> createOutputBufferFor(CLContext context, int size);
	
	public CLKernel.LocalSize createSharedBufferFor(CLContext context, String type, int size);

	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev,
			int size);

	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev);

	public CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size);
}
