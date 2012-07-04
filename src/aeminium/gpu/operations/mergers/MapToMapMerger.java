package aeminium.gpu.operations.mergers;

import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lazyness.LazyPList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaNoSeedReducer;

public class MapToMapMerger<I, M, O> {
	
	private Map<I,M> first;
	private Map<M,O> second;
	private PList<I> current;
	
	public MapToMapMerger(Map<I,M> f, Map<M,O> s, PList<I> c) {
		first = f;
		second = s;
		current = c;
	}

	public PList<O> getOutput() {
		LazyEvaluator<O> eval = new LazyEvaluator<O>() {

			private Map<I,O> fakeMap() {
				LambdaMapper<I,O> fakeLambda = new LambdaMapper<I, O>() {

					@Override
					public O map(I o) {
						return second.getMapFun().map(first.getMapFun().map(o));
					}

					@Override
					public String getSource() {
						return String.format("return %s(%s(input));", second.getMapOpenCLName(), first.getMapOpenCLName());
					}
				};
				
				StringBuilder extraCode = new StringBuilder();
				extraCode.append(first.getOtherSources());
				extraCode.append(second.getOtherSources());
				extraCode.append(first.getMapOpenCLSource());
				extraCode.append(second.getMapOpenCLSource());
				Map<I,O> op = new Map<I,O>(fakeLambda,current,extraCode.toString(), first.getDevice()) {
					public String getOutputType() {
						return second.getOutputType();
					}
				};
				return op;
			}
			
			@Override
			public PList<O> evaluate() {
				Map<I,O> op = fakeMap();
				return op.getOutput();
			}

			@Override
			public Class<?> getType() {
				return current.getType();
			}

			@Override
			public <O2> boolean canMergeWithMap(LambdaMapper<O, O2> mapFun) {
				return true;
			}

			@Override
			public <O2> PList<O2> mergeWithMap(Map<O, O2> mapOp) {
				MapToMapMerger<I,O,O2> merger = new MapToMapMerger<I,O,O2>(fakeMap(), mapOp, current);
				return merger.getOutput();
			}

			@Override
			public boolean canMergeWithReduce(LambdaNoSeedReducer<O> reduceFun) {
				return true;
			}

			@Override
			public O mergeWithReducer(Reduce<O> reduceOp) {
				MapToReduceMerger<I,O> merger = new MapToReduceMerger<I,O>(fakeMap(), reduceOp, current);
				return merger.getOutput();
			}
		};
		return new LazyPList<O>(eval, first.getOutputSize());
	}

}
