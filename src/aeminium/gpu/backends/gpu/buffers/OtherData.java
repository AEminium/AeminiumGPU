package aeminium.gpu.backends.gpu.buffers;

import java.lang.reflect.Field;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

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
	
	public static OtherData[] extractOtherData(Object fun) {
		Field[] fs = fun.getClass().getFields();
		OtherData[] otherData = new OtherData[fs.length];
		int i = 0;
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				otherData[i++] = new OtherData(f.getName(), (PObject) f.get(fun));
			} catch (IllegalArgumentException e) {
				// Avoided
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// Avoided
				e.printStackTrace();
			}
		}
		return otherData;
	}
}
