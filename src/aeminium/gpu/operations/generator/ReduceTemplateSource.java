package aeminium.gpu.operations.generator;

import aeminium.gpu.operations.functions.LambdaNoSeedReducer;


public interface ReduceTemplateSource<T> {

	String getInputType();

	String getOutputType();

	LambdaNoSeedReducer<T> getReduceFun();

	String getOpenCLSeed();

	String getOtherSources();

}
