package aeminium.gpu.tests.lists;

import aeminium.gpu.lists.LongList;
import aeminium.gpu.lists.PList;

public class TestLongList extends TestAbstractListTemplate<Long> {
	
	@Override
	PList<Long> getEmptyList() {
		return new LongList();
	}
	
	protected Long getSomeValue() {
		return 222L;
	}

	@Override
	Long getSomeOtherValue() {
		return 111L;
	}

	@Override
	PList<Long> createBigList(int size, int delta) {
		long[] memory = new long[size-delta];
		long b = 111111L;
		LongList fl = new LongList(memory,size-delta);
		for (int i = size-delta; i < size+delta; i++) {
			fl.add(i+b);
		}
		return fl;
	}
}
