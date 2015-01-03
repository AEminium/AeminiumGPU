package aeminium.gpu.operations;

import aeminium.gpu.backends.cpu.CPUMap;
import aeminium.gpu.backends.gpu.GPUMap;
import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lazyness.LazyPList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.operations.contracts.GenericProgram;
import aeminium.gpu.operations.contracts.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import aeminium.gpu.operations.mergers.MapToMapMerger;
import aeminium.gpu.operations.mergers.MapToReduceMerger;
import aeminium.gpu.utils.ExtractTypes;

public class Map<I, O> extends GenericProgram implements Program {

	protected PList<I> input;
	private PList<O> output;
	protected LambdaMapper<I, O> mapFun;
	
	
	protected GPUMap<I, O> gpuOp;
	protected CPUMap<I, O> cpuOp;
	

	// Constructors

	public Map(LambdaMapper<I, O> mapFun2, PList<I> list, GPUDevice dev) {
		this(mapFun2, list, "", dev);
	}

	public Map(LambdaMapper<I, O> mapFun, PList<I> list, String other,
			GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.mapFun = mapFun;
		
		cpuOp = new CPUMap<I, O>(input, mapFun);
		gpuOp = new GPUMap<I, O>(input, mapFun, other);
		gpuOp.setDevice(dev);
	}

	protected int getParallelUnits() {
		return input.size();
	}
	
	@Override
	protected int getBalanceSplitPoint() {
		return OpenCLDecider.getSplitPoint(input.size(), input.size(), input.size(),
				mapFun.getSource(), mapFun.getSourceComplexity());
	}
	

	@Override
	public void cpuExecution(int start, int end) {
		cpuOp.setLimits(start, end);
		cpuOp.execute();
	}

	@Override
	public void gpuExecution(int start, int end) {
		gpuOp.setLimits(start, end);
		gpuOp.execute();
	}

	@Override
	protected void mergeResults(boolean hasGPU, boolean hasCPU) {
		if (!hasGPU) {
			cpuOp.waitForExecution();
			output = cpuOp.getOutput();
		} else if (!hasCPU) {
			gpuOp.waitForExecution();
			output = gpuOp.getOutput();
		} else {
			gpuOp.waitForExecution();
			cpuOp.waitForExecution();
			output = gpuOp.getOutput();
			output.extend(cpuOp.getOutput());
		}
	}

	// Output

	public PList<O> getOutput() {
		final Map<I, O> innerMap = this;

		// Lazy return
		LazyEvaluator<O> operation = new LazyEvaluator<O>() {

			@Override
			public PList<O> evaluate() {
				innerMap.execute();
				return output;
			}

			@Override
			public Class<?> getType() {
				return BufferHelper.getClassOf(ExtractTypes.getMapOutputType(mapFun, input));
			}

			@Override
			public <K> boolean canMergeWithMap(LambdaMapper<O, K> mapFun) {
				return true;
			}

			@Override
			public <K> PList<K> mergeWithMap(Map<O, K> mapOp) {
				MapToMapMerger<I, O, K> merger = new MapToMapMerger<I, O, K>(
						innerMap, mapOp, input);
				return merger.getOutput();
			}

			@Override
			public boolean canMergeWithReduce(LambdaReducerWithSeed<O> reduceFun) {
				return true;
			}

			@Override
			public O mergeWithReducer(Reduce<O> reduceOp) {
				MapToReduceMerger<I, O> merger = new MapToReduceMerger<I, O>(
						innerMap, reduceOp, input);
				return merger.getOutput();
			}

		};
		return new LazyPList<O>(operation, input.size());
	}


	public int getOutputSize() {
		return input.size();
	}

	// Getters and Setters

	public void setOutput(PList<O> output) {
		this.output = output;
	}

	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}

	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}

	public GPUMap<I, O> getGPUMap() {
		return gpuOp;
	}
	
	


}
