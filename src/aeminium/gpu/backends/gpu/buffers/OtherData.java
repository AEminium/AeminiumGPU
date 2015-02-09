package aeminium.gpu.backends.gpu.buffers;

import java.lang.reflect.Field;
import java.util.ArrayList;

import aeminium.gpu.collections.PNativeWrapper;
import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.AbstractMatrix;
import aeminium.gpu.collections.matrices.PMatrix;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLQueue;

public class OtherData {
	public String name;
	public PObject obj;
	public CLBuffer<?> buffer;
	public String type;
	public Field f;
	
	public OtherData(Field f, String n, PObject o) {
		this.f = f;
		this.name = n;
		this.obj = o;
		this.type = obj.getCLType();
	}
	
	public void createBuffer(CLContext ctx) {
		if (!isNative()) {
			buffer = BufferHelper.createInputOutputBufferFor(ctx, obj);
		}
	}
	
	public CLBuffer<?> getBuffer() {
		return buffer;
	}
	
	public boolean isNative() {
		return obj.isNative();
	}
	
	public static ArrayList<OtherData> extractOtherData(Object fun, Object fun2) {
		Field[] fs = fun.getClass().getFields();
		Field[] fs2 = fun2.getClass().getFields();
		ArrayList<OtherData> otherData = new ArrayList<OtherData>();
		int i = 0;
		i = fill(otherData, fun, fs, i);
		i = fill(otherData, fun2, fs2, i);
		return otherData;
	}
	
	public static ArrayList<OtherData> extractOtherData(Object fun) {
		Field[] fs = fun.getClass().getFields();
		ArrayList<OtherData> otherData = new ArrayList<OtherData>();
		int i = 0;
		i = fill(otherData, fun, fs, i);
		return otherData;
	}

	private static int fill(ArrayList<OtherData> otherData, Object oi, Field[] fs, int i) {
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				Object o = f.get(oi);
				if (o instanceof Integer) o = new PNativeWrapper<Integer>((Integer) o);
				if (o instanceof Double) o = new PNativeWrapper<Double>((Double) o);
				otherData.add(new OtherData(f, f.getName(), (PObject) o));
				if (o instanceof AbstractMatrix) {
					otherData.add(new OtherData(f, "__" + f.getName() + "_cols", new PNativeWrapper<Integer>(((AbstractMatrix<?>) o).cols())));
				}
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

	public void setArg(CLKernel kernel, int i) {
		if (!isNative()) {
			kernel.setArg(i, getBuffer());
			return;
		}
		PNativeWrapper<?> wrapper = (PNativeWrapper<?>) obj;
		Object c = wrapper.getVal();
		
		if (c instanceof Integer) kernel.setArg(i, ((Integer) c).intValue());
		if (c instanceof Double) kernel.setArg(i, ((Double) c).doubleValue());
		if (c instanceof Float) kernel.setArg(i, ((Float) c).floatValue());
		
	}

	public void readFromBuffer(CLContext ctx, CLQueue q) {
		if (!isNative()) {
			Object target;
			PList<?> newList = BufferHelper.extractFromBuffer(buffer, q, null, obj.getClass().getSimpleName(), buffer.getElementSize());
			if (obj instanceof AbstractMatrix) {
				PMatrix<?> newMatrix = CollectionFactory.matrixfromPList(newList, ((AbstractMatrix<?>) obj).rows(), ((AbstractMatrix<?>) obj).cols());
				target = newMatrix;
			} else {
				target = newList;
			}
			try {
				f.set(name, target);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
