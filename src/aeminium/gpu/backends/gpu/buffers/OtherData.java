package aeminium.gpu.backends.gpu.buffers;

import java.lang.reflect.Field;

import aeminium.gpu.collections.PObject;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;

public class OtherData {
	public String name;
	public PObject obj;
	public CLBuffer<?> buffer;
	public String type;
	
	public OtherData(String n, PObject o) {
		this.name = n;
		this.obj = o;
		this.type = obj.getCLType();
	}
	
	public void createBuffer(CLContext ctx) {
		buffer = BufferHelper.createInputOutputBufferFor(ctx, obj);
	}
	
	public CLBuffer<?> getBuffer() {
		return buffer;
	}
	
	public static OtherData[] extractOtherData(Object fun, Object fun2) {
		Field[] fs = fun.getClass().getFields();
		Field[] fs2 = fun2.getClass().getFields();
		OtherData[] otherData = new OtherData[fs.length + fs2.length];
		int i = 0;
		i = fill(otherData, fun, fs, i);
		i = fill(otherData, fun2, fs2, i);
		return otherData;
	}
	
	public static OtherData[] extractOtherData(Object fun) {
		Field[] fs = fun.getClass().getFields();
		OtherData[] otherData = new OtherData[fs.length];
		int i = 0;
		i = fill(otherData, fun, fs, i);
		return otherData;
	}

	private static int fill(OtherData[] otherData, Object o, Field[] fs, int i) {
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				otherData[i++] = new OtherData(f.getName(), (PObject) f.get(o));
			} catch (IllegalArgumentException e) {
				// Avoided
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// Avoided
				e.printStackTrace();
			}
		}
		return i;
	}
}
