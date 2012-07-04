package aeminium.gpu.operations.mergers;

import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.MapReduce;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaNoSeedReducer;

public class MapToReduceMerger<I,O> {
	
	private Map<I,O> first;
	private Reduce<O> second;
	private PList<I> current;
	
	public MapToReduceMerger(Map<I,O> f, Reduce<O> s, PList<I> c) {
		first = f;
		second = s;
		current = c;
	}

	@SuppressWarnings("unchecked")
	public O getOutput() {
		LazyEvaluator<O> eval = new LazyEvaluator<O>() {
			
			@Override
			public O evaluate() {
				StringBuilder extraCode = new StringBuilder();
				extraCode.append(first.getOtherSources());
				extraCode.append(second.getOtherSources());
				MapReduce<I,O> op = new MapReduce<I,O>(first.getMapFun(), second.getReduceFun(), current, extraCode.toString(), first.getDevice());
				return op.getOutput();
			}

			@Override
			public Class<?> getType() {
				return current.getType();
			}

			@Override
			public <O2> boolean canMergeWithMap(LambdaMapper<O, O2> mapFun) {
				return false;
			}

			@Override
			public <O2> PList<O2> mergeWithMap(Map<O, O2> mapOp) {
				return null;
			}

			@Override
			public boolean canMergeWithReduce(LambdaNoSeedReducer<O> reduceFun) {
				return false;
			}

			@Override
			public O mergeWithReducer(Reduce<O> reduceOp) {
				return null;
			}


		};
		return (O) eval.evaluate();
	}
}
