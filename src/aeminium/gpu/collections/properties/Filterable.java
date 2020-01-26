package aeminium.gpu.collections.properties;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;

public interface Filterable<I> {
    PList<I> filter(LambdaFilter<I> filter);
}
