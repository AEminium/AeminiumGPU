package aeminium.gpu.collections.lazyness;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.AbstractList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.Filter;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class LazyPList<T> extends AbstractList<T> implements PList<T> {

	private boolean evaluated = false;
	private PList<T> actual;
	private LazyEvaluator<T> evaluator;
	private int lazynessLevel = 1;

	public LazyPList(LazyEvaluator<T> eval, int size) {
		super();
		this.size = size;
		this.evaluator = eval;
	}

	@SuppressWarnings("unchecked")
	public PList<T> evaluate() {
		if (!evaluated) {
			actual = (PList<T>) evaluator.evaluate();
			evaluated = true;
		}
		return actual;
	}

	@Override
	public int size() {
		if (evaluated) {
			return actual.size();
		} else {
			return size;
		}
	}

	@Override
	public void add(int index, T e) {
		evaluate();
		actual.add(index, e);
	}

	@Override
	public void remove(T o) {
		evaluate();
		actual.remove(o);
	}

	@Override
	public T get(int index) {
		evaluate();
		return actual.get(index);
	}

	@Override
	public void set(int index, T e) {
		evaluate();
		actual.set(index, e);
	}

	@Override
	public T remove(int index) {
		evaluate();
		return actual.remove(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		size = 0;
		evaluated = true;
		actual = (PList<T>) CollectionFactory.listFromType(getContainingType()
				.getSimpleName().toString());
	}

	@Override
	public PList<T> subList(int fromIndex, int toIndex) {
		evaluate();
		return actual.subList(fromIndex, toIndex);
	}

	@Override
	public Class<?> getContainingType() {
		return evaluator.getType();
	}

	@Override
	public <O> PList<O> map(LambdaMapper<T, O> mapFun) {
		if (!evaluated && evaluator.canMergeWithMap(mapFun)) {
			Map<T, O> m = new Map<T, O>(mapFun, this, this.getDevice());
			LazyPList<O> r = (LazyPList<O>) evaluator.mergeWithMap(m);
			r.setLazynessLevel(lazynessLevel + 1);
			return r;
		} else {
			evaluate();
			return actual.map(mapFun);
		}
	}

    @Override
    public PList<T> filter(LambdaFilter<T> filterFun) {
        if (!evaluated && evaluator.canMergeWithFilter(filterFun)) {
            Filter<T> f = new Filter<T>(filterFun, this, this.getDevice());
            LazyPList<T> r = (LazyPList<T>) evaluator.mergeWithFilter(f);
            r.setLazynessLevel(lazynessLevel + 1);
            return r;
        } else {
            evaluate();
            return actual.filter(filterFun);
        }
    }

	@Override
	public T reduce(LambdaReducerWithSeed<T> reducer) {
		if (!evaluated && evaluator.canMergeWithReduce(reducer)) {
			Reduce<T> m = new Reduce<T>(reducer, this, this.getDevice());
			return evaluator.mergeWithReducer(m);
		} else {
			evaluate();
			return actual.reduce(reducer);
		}
	}

	public int getLazynessLevel() {
		return lazynessLevel;
	}

	public void setLazynessLevel(int lazynessLevel) {
		this.lazynessLevel = lazynessLevel;
	}

	@Override
	public PList<T> extend(PList<T> extra) {
		evaluate();
		return actual.extend(extra);
	}

    @Override
	public PList<T> extendAt(int i, PList<T> extra) {
		evaluate();
		return actual.extendAt(i, extra);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replaceBy(PList<?> newList) {
		actual = (PList<T>) newList;
	}

	@Override
	public PObject copy() {
		this.evaluate();
		return actual.copy();
	}
}
