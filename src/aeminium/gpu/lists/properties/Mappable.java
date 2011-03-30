package aeminium.gpu.lists.properties;

import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public interface Mappable<I> {
	public <O> PList<O> map(LambdaMapper<I,O> mapper);
}
