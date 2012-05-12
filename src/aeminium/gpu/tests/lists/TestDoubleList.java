package aeminium.gpu.tests.lists;

import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.PList;

public class TestDoubleList extends TestAbstractListTemplate<Double> {

	@Override
	PList<Double> getEmptyList() {
		return new DoubleList();
	}

	protected Double getSomeValue() {
		return 1.000001;
	}

	@Override
	Double getSomeOtherValue() {
		return 4.0000001;
	}

	@Override
	PList<Double> createBigList(int size, int delta) {
		double[] memory = new double[size - delta];
		double b = 2.000005;
		DoubleList fl = new DoubleList(memory, size - delta);
		for (int i = size - delta; i < size + delta; i++) {
			fl.add(i + b);
		}
		return fl;
	}
}
