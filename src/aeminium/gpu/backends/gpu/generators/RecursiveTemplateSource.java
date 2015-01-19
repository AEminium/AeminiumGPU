package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.operations.functions.AbstractRecursiveStrategy;

public interface RecursiveTemplateSource<R extends Number,R2, T> {
	String getRType();
	String getR2Type();
	String getTType();
	
	AbstractRecursiveStrategy<R, R2, T> getRecursiveStrategy();
}
