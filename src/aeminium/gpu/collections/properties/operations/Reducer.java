package aeminium.gpu.collections.properties.operations;

public interface Reducer<I> {
	public I combine(I input, I other);
	public I getSeed();
}