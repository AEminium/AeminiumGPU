package aeminium.gpu.collections.properties.operations;

public interface NoSeedReducer<I> {
	public I combine(I input, I other);
}
