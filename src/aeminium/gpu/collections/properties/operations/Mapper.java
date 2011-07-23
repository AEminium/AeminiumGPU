package aeminium.gpu.collections.properties.operations;

public interface Mapper<I,O> {
	public O map(I input);
}