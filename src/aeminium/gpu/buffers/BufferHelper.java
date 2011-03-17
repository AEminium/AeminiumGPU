package aeminium.gpu.buffers;

import java.util.HashMap;

import aeminium.gpu.lists.BooleanList;
import aeminium.gpu.lists.PList;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class BufferHelper {
	
	private static HashMap<String, IBufferFactory> factories = new HashMap<String, IBufferFactory>();

	private static HashMap<String, String> clTypes = new HashMap<String, String>();
	
	static {
		factories.put("Integer", new IntBufferFactory());
		factories.put("Float", new FloatBufferFactory());
		factories.put("Double", new DoubleBufferFactory());
		factories.put("Character", new CharBufferFactory());
		factories.put("Long", new LongBufferFactory());
		factories.put("Boolean", new CharBufferFactory());
		
		clTypes.put("Integer", "int");
		clTypes.put("Float", "float");
		clTypes.put("Double", "double");
		clTypes.put("Character", "char");
		clTypes.put("Long", "long");
		clTypes.put("Boolean", "char");
	}
	
	private static <T> IBufferFactory getFactory(PList<T> list) {
		return getFactory(list.getType().getSimpleName());
	}
	
	private static <T> IBufferFactory getFactory(String type) {
		IBufferFactory f = factories.get(type);
		if (f == null) {
			System.out.println("No buffer for type " + type + ".");
			System.exit(1);
		}
		return f;
	}
	
	public static <T> CLBuffer<?> createInputBufferFor(CLContext context, PList<T> list) {
		 IBufferFactory f = getFactory(list);
		 return f.createInputBufferFor(context, list);
	}

	public static <T> CLBuffer<?> createOutputBufferFor(CLContext context, PList<T> list, int size) {
		return createOutputBufferFor(context, list.getClass().getSimpleName(), size);
	}
	
	public static CLBuffer<?> createSharedBufferFor(CLContext context, String outputType, int size) {
		IBufferFactory f = getFactory(outputType);
		return f.createSharedBufferFor(context, outputType, size);
	}
	
	public static <T> CLBuffer<?> createOutputBufferFor(CLContext context, String type, int size) {
		 IBufferFactory f = getFactory(type);
		 return f.createOutputBufferFor(context, size);
	}
	
	public static PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev, int size, PList<?> list) {
		return extractFromBuffer(outbuffer, q, ev, list.getType().getSimpleName(), size);
	}
	
	public static PList<?> extractFromBuffer(CLBuffer<?> outbuffer, CLQueue q, CLEvent ev, String type, int size) {
		IBufferFactory f = getFactory(type);
		return f.extractFromBuffer(outbuffer, q, ev, size);
	}

	public static String getCLTypeOf(String type) {
		return clTypes.get(type);
	}
	
	public static Object decode(Object in, String outputType) {
		if (outputType.equals("Boolean") && in instanceof Character) {
			return BooleanList.decode((Character) in);
		}
		return in;
	}
	
}
