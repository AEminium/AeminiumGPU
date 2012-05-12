package aeminium.gpu.collections.properties.operations;

public interface ReducerWithSeed<I> extends Reducer<I>{
	public I getSeed();
}