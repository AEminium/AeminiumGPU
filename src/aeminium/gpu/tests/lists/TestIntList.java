package aeminium.gpu.tests.lists;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;

public class TestIntList extends TestAbstractListTemplate<Integer> {
	
	@Override
	PList<Integer> getEmptyList() {
		return new IntList();
	}
	
	protected Integer getSomeValue() {
		return 2;
	}

	@Override
	Integer getSomeOtherValue() {
		return 13;
	}

	@Override
	PList<Integer> createBigList(int size, int delta) {
		int[] memory = new int[size-delta];
		int b = 3;
		IntList fl = new IntList(memory,size-delta);
		for (int i = size-delta; i < size+delta; i++) {
			fl.add(i+b);
		}
		return fl;
	}
}
