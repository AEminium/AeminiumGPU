package aeminium.gpu.collections.properties;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public interface Mappable<I> {
	public <O> PList<O> map(LambdaMapper<I,O> mapper);
}
