package aeminium.gpu.collections.lazyness.helpers;

import aeminium.gpu.operations.functions.LambdaMapper;

public class IdentityMapper<T> extends LambdaMapper<T,T> {
	@Override
	public T map(T input) {
		return input;
	}
	
	@Override
	public String getSource() {
		return "return input;";
	}
}
