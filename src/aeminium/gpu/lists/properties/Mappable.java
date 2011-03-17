package aeminium.gpu.lists.properties;

import aeminium.gpu.lists.PList;
import aeminium.gpu.lists.properties.operations.Mapper;

public interface Mappable<I> {
	public <O> PList<O> map(Mapper<I,O> mapper);
}
