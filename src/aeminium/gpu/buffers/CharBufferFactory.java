package aeminium.gpu.buffers;

import org.bridj.Pointer;

import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.LocalSize;

public class CharBufferFactory implements IBufferFactory {

	public static byte[] encodeCharToBytes(char[] c) {
		byte[] b = new byte[c.length];
		for (int i = 0; i < c.length; i++)
			b[i] = (byte) (c[i] & 0x007F);

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
		return context.createByteBuffer(CLMem.Usage.Input,
				Pointer.pointerToBytes(car), true);
	}

	@Override
	public <T> CLBuffer<?> createInputOutputBufferFor(CLContext context,
			PList<T> list) {
		char[] ar = ((CharList) list).getArray();
		byte[] car = encodeCharToBytes(ar);

		Pointer<Byte> ptr = Pointer.allocateBytes(list.size()).order(
				context.getByteOrder());
		ptr.setBytes(car);
		return context.createBuffer(CLMem.Usage.InputOutput, ptr, true);
	}

	@Override
	public CLBuffer<?> createOutputBufferFor(CLContext context, int size) {
		Pointer<Byte> ptr = Pointer.allocateBytes(size).order(
				context.getByteOrder());
		return context.createBuffer(CLMem.Usage.Output, ptr, true);
	}

	@Override
	public LocalSize createSharedBufferFor(CLContext context, String type,
			int size) {
		return new LocalSize(size);
	}

	@Override
	public PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev, int size) {
		byte[] pcontent = outbuffer.read(q, ev).getBytes();
		char[] content = decodeBytesToChar(pcontent);
		return new CharList(content, size);
	}

	@Override
	public Object extractElementFromBuffer(CLBuffer<?> outbuffer, CLQueue q,
			CLEvent ev) {
		char[] content = decodeBytesToChar(outbuffer.read(q, ev).getBytes(1));
		return content[0];
	}

	@Override
	public CLBuffer<?> createInputOutputBufferFor(CLContext context,
			String outputType, int size) {
		return context.createByteBuffer(CLMem.Usage.InputOutput, size);
	}

}
