package aeminium.gpu.buffers;

import java.nio.ByteBuffer;

import aeminium.gpu.lists.CharList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class CharBufferFactory implements IBufferFactory {

	public static byte[] encodeCharToBytes(char[] c) {
	    byte[] b = new byte[c.length];
	    for (int i = 0; i < c.length; i++)
	      b[i] = (byte)(c[i] & 0x007F);

	    return b;
	}
	
	public static char[] decodeBytesToChar(byte[] ascii) {
		String tmp = new String(ascii);
		return tmp.toCharArray();
	}
	
	
	@Override
	public <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		char[] ar = ((CharList) list).getArray();
		byte[] car = encodeCharToBytes(ar);
		ByteBuffer ibuffer = ByteBuffer.wrap(car, 0, list.size());
		return context.createByteBuffer(CLMem.Usage.Input, ibuffer, true);
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
		byte[] pcontent = new byte[size];
		outbuffer.asCLByteBuffer().read(q, ev).get(pcontent);
		char[] content = decodeBytesToChar(pcontent);
		return new CharList(content, size);
	}
	
	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev) {
		byte[] pcontent = new byte[1];
		outbuffer.asCLByteBuffer().read(q, ev).get(pcontent);
		char[] content = decodeBytesToChar(pcontent);
		return content[0];
	}

}
