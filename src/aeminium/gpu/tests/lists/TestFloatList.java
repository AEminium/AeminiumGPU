package aeminium.gpu.tests.lists;

import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.PList;

public class TestFloatList extends TestAbstractListTemplate<Float> {

	@Override
	PList<Float> getEmptyList() {
		return new FloatList();
	}

	protected Float getSomeValue() {
		return 1.1f;
	}

	@Override
	Float getSomeOtherValue() {
		return 4.3f;
	}

	@Override
	PList<Float> createBigList(int size, int delta) {
		float[] memory = new float[size - delta];
		float b = 0.5f;
		FloatList fl = new FloatList(memory, size - delta);
		for (int i = size - delta; i < size + delta; i++) {
			fl.add(i + b);
		}
		return fl;
	}
}
