package aeminium.gpu.utils;

import java.lang.reflect.Method;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class ExtractTypes {
	public static String extractReturnTypeOutOf(Object target, String methodName) {
		Class<?> klass = target.getClass();
		for (Method m : klass.getMethods()) {
			if (m.getName().equals(methodName)) {
				String pname = m.getReturnType().getSimpleName().toString();

				// Ignore Object types.
				if (!pname.equals("Object")) {
					return pname;
				}
			}
		}
		System.out.println("AeminiumGPU doesn't support Generic Lambdas.");
		return null;
	}
	
	
	public static Class<?> getMapOutputClass(LambdaMapper<?,?> mapFun, Class<?> inputType) {
		Class<?> klass = mapFun.getClass();
		try {
			return klass.getMethod("map", inputType).getReturnType();
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			System.out.println("AeminiumGPU Runtime does not support generic types on Lambdas.");
			System.out.println(mapFun.getSource());
			return null;
		}
	}
	
	public static String getMapOutputType(LambdaMapper<?,?> mapFun, PList<?> input) {
		Class<?> type = getMapOutputClass(mapFun, input.getType());
		if (type == null) return null;
		return type.getSimpleName().toString();
	}
	
	public static String getMapOutputType(LambdaMapper<?,?> mapFun, LambdaMapper<?,?> mapFun2, PList<?> input) {
		Class<?> ft = getMapOutputClass(mapFun, input.getType());
		Class<?> st = getMapOutputClass(mapFun2, ft);
		if (st == null) return null;
		return st.getSimpleName().toString();
	}
}
