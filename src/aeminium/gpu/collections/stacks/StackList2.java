package aeminium.gpu.collections.stacks;

import aeminium.gpu.collections.PObject;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.operations.functions.LambdaFilter;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

import java.util.Iterator;

public class StackList2<A,B> implements PList<Stack2<A,B>> {
	PList<A> as;
	PList<B> bs;
	PList<Integer> bStart;
	PList<Integer> bEnd;
	
	public StackList2(PList<A> as, PList<B> bs) {
		this.as = as;
		this.bs = bs;
	}
	
	public StackList2(PList<A> as, PList<B> bs, PList<Integer> bIndex, PList<Integer> bEnd) {
		this.as = as;
		this.bs = bs;
		this.bStart = bIndex;
		this.bEnd = bEnd;
	}
	
	
	@SuppressWarnings("unchecked")
	public StackList2(String t1, String t2) {
		as = (PList<A>) CollectionFactory.listFromType(t1);
		if (t2.endsWith("List")) {
			bs = (PList<B>) CollectionFactory.listFromType(t2.replace("List", ""));
			bStart = new IntList();
		} else {
			bs = (PList<B>) CollectionFactory.listFromType(t2);
		}
	}

	@Override
	public int size() {
		return as.size();
	}

	@Override
	public Class<?> getContainingType() {
		return Stack2.class;
	}

	@Override
	public String getCLType() {
		return as.getCLType() + ";" + bs.getCLType();
	}

	@Override
	public boolean isNative() {
		return false;
	}

	@Override
	public PObject copy() {
		return null;
	}

	@Override
	public <O> PList<O> map(LambdaMapper<Stack2<A, B>, O> mapper) {
		throw new RuntimeException("Not implemented");
	}

    @Override
    public PList<Stack2<A, B>> filter(LambdaFilter<Stack2<A, B>> filter) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public Stack2<A, B> reduce(LambdaReducerWithSeed<Stack2<A, B>> reducer) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public PMatrix<Stack2<A, B>> groupBy(int n) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Iterator<Stack2<A, B>> iterator() {
		return new Iterator<Stack2<A, B>>() {
			
			private int counter = 0;
			private int size = as.size();

			@Override
			public boolean hasNext() {
				return counter < size;
			}

			@Override
			public Stack2<A, B> next() {
				return new Stack2<A,B>(as.get(counter), bs.get(counter++));
			}
			
		};
	}

	@Override
	public int length() {
		return as.length();
	}

	@Override
	public boolean isEmpty() {
		return as.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(Stack2<A, B> e) {
		as.add(e.get1());
		if (bStart == null) {
			bs.add(e.get2());
		} else {
			PList<B> l = (PList<B>) e.get2();
			bEnd.add(bStart.size() + l.size());
			bStart.add(bStart.size());
			bs.extend(l);
		}
	}

	@Override
	public void add(int index, Stack2<A, B> e) {
		if (index < size()) {
			add(e);
			return;
		}
		
		as.add(index, e.get1());
		if (bStart == null) {
			bs.add(index, e.get2());
		} else {
			@SuppressWarnings("unchecked")
			PList<B> appendix = (PList<B>) e.get2();
			bStart.add(index, bStart.get(index) + appendix.size());
			bEnd.add(index, bEnd.get(index) + appendix.size());
			bStart.add(bStart.size());
			for (int i=index; i<bStart.size();i++) {
				bStart.set(i, bStart.get(i) + appendix.size());
				bEnd.set(i, bEnd.get(i) + appendix.size());
			}
			int i = 0;
			for (B b : appendix) {
				bs.add(index+i, b);
				i++;
			}
		}
	}

	@Override
	public void remove(Stack2<A, B> o) {
		as.remove(o.get1());
		bs.remove(o.get2());
	}

	@Override
	public Stack2<A, B> remove(int index) {
		A a = as.remove(index);
		B b = bs.remove(index);
		return new Stack2<A,B>(a,b);
	}

	@Override
	public Stack2<A, B> get(int index) {
		A a = as.get(index);
		B b = bs.get(index);
		return new Stack2<A,B>(a,b);
	}

	@Override
	public void set(int index, Stack2<A, B> e) {
		as.set(index, e.get1());
		bs.set(index, e.get2());
	}

	@Override
	public void clear() {
		as.clear();
		bs.clear();
	}

	@Override
	public PList<Stack2<A, B>> subList(int fromIndex, int toIndex) {
		return new StackList2<A,B>(as.subList(fromIndex, toIndex), bs.subList(fromIndex, toIndex));
	}

	@Override
	public PList<Stack2<A, B>> evaluate() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PList<Stack2<A, B>> extend(PList<Stack2<A, B>> extra) {
		as.extend(((StackList2<A, B>) extra).getAs());
		bs.extend(((StackList2<A, B>) extra).getBs());
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PList<Stack2<A, B>> extendAt(int i, PList<Stack2<A, B>> extra) {
		as.extend(((StackList2<A, B>) extra).getAs());
		bs.extend(((StackList2<A, B>) extra).getBs());
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replaceBy(PList<?> newList) {
		as = ((StackList2<A, B>) newList).getAs();
		bs = ((StackList2<A, B>) newList).getBs();
	}
	
	public PList<A> getAs() {
		return as;
	}
	
	public PList<B> getBs() {
		return bs;
	}
}
