package aeminium.gpu.backends.gpu.buffers;

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
		this.type = BufferHelper.getCLTypeOfObject(obj);
	}
	
	public void createBuffer(CLContext ctx) {
		buffer = BufferHelper.createInputOutputBufferFor(ctx, obj);
	}
	
	public CLBuffer<?> getBuffer() {
		return buffer;
	}
}
