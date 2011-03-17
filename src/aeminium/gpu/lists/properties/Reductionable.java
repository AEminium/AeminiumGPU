package aeminium.gpu.lists.properties;

import aeminium.gpu.lists.properties.operations.Reducer;

public interface Reductionable<T> {
	public T reduce(Reducer<T> reducer);
}
