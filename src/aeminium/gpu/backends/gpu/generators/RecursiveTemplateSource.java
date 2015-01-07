package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.operations.functions.RecursiveStrategy;

public interface RecursiveTemplateSource<R extends Number,T> {
	String getRType();
	String getTType();
	
	RecursiveStrategy<R, T> getRecursiveStrategy();
}
