package aeminium.gpu.backends.cpu;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class CPUReduce<I, O> extends CPUGenericKernel {
	protected PList<I> input;
	protected O output;
	protected LambdaMapper<I, O> mapFun;
	protected LambdaReducerWithSeed<O> reduceFun;
	
	public CPUReduce(PList<I> input, LambdaReducerWithSeed<O> reduceFun) {
		this(input, new LambdaMapper<I, O>() {
			@SuppressWarnings("unchecked")
			@Override
			public O map(I input) {
				return (O) input;
			}
		}, reduceFun);
	}
	
	public CPUReduce(PList<I> input, LambdaMapper<I,O> mapper, LambdaReducerWithSeed<O> reduceFun) {
		this.input = input;
		this.reduceFun = reduceFun;
		this.mapFun = mapper;
	}
	
	@Override
	public void execute() {
		for (int i=start; i < end; i++) {
			output = reduceFun.combine(mapFun.map(input.get(i)), output);
		}
	}
	
	@Override
	public void waitForExecution() {
		
	}
	
	public O getOutput() {
		return output;
	}

	public void setOutput(O output) {
		this.output = output;
	}

}
