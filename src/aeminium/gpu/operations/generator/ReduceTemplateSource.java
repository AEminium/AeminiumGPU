package aeminium.gpu.operations.generator;

import aeminium.gpu.operations.functions.LambdaReducer;


public interface ReduceTemplateSource<T> {

	String getInputType();

	String getOutputType();

	LambdaReducer<T> getReduceFun();

	String getOpenCLSeed();

	String getOtherSources();

}
