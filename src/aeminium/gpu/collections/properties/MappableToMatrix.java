package aeminium.gpu.collections.properties;

import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.operations.functions.LambdaMapper;

public interface MappableToMatrix<I> {
	public <O> PMatrix<O> map(LambdaMapper<I, O> mapper);
}
