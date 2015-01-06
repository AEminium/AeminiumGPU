package aeminium.gpu.backends.mcpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.CPUDevice;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.utils.ExtractTypes;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;

public class MCPUMap<I,O> extends MCPUGenericKernel {

	protected Task task;
	
	protected PList<I> input;
	protected PList<O> output;
	protected LambdaMapper<I, O> mapFun;
	
	protected String outputType;
	
	
	public MCPUMap(PList<I> input, LambdaMapper<I, O> mapFun) {
		this.input = input;
		this.mapFun = mapFun;
		outputType = mapFun.getOutputType();
		if (outputType == null) {
			outputType = ExtractTypes.getMapOutputType(mapFun, input);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		output = (PList<O>) CollectionFactory.listFromType(outputType);
		final int start = this.start;
		task = ForTask.createFor(CPUDevice.rt, new aeminium.runtime.helpers.loops.Range(start, input.size()), new ForBody<Integer>() {

			@Override
			public void iterate(Integer i, aeminium.runtime.Runtime rt,
					Task current) {
				output.set(i - start, mapFun.map(input.get(i)));
			}
			
		}, Runtime.NO_HINTS);
		CPUDevice.submit(task);
	}

	@Override
	public void waitForExecution() {
		CPUDevice.waitFor(task);
	}
	
	public PList<O> getOutput() {
		return output;
	}
	
	public PList<I> getInput() {
		return input;
	}


	public void setInput(PList<I> input) {
		this.input = input;
	}


	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}


	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}


	public void setOutput(PList<O> output) {
		this.output = output;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

}
