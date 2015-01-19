package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.operations.functions.Recursive2DStrategy;

public interface RecursiveTemplateSource<R extends Number,R2, T> {
	String getRType();
	String getR2Type();
	String getTType();
	
	Recursive2DStrategy<R, R2, T> getRecursiveStrategy();
}
