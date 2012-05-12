package aeminium.gpu.collections.properties.operations;

public interface Reducer<I> extends NoSeedReducer<I>{
	public I getSeed();
}