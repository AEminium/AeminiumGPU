package aeminium.gpu.operations.utils;

import java.lang.reflect.Method;

public class ExtractTypes {
	public static String extractReturnTypeOutOf(Object target, String methodName) {
		Class<?> klass = target.getClass();
		for (Method m: klass.getMethods()) {
			if (m.getName().equals(methodName)) {
				String pname = m.getReturnType().getSimpleName().toString();
				
				// Ignore Object types.
				if (!pname.equals("Object")) {
					return pname;
				}
			}
		}
		System.out.println("AeminiumGPU doesn't support Generic Lambdas.");
		System.exit(0);
		return null;
	}
}
