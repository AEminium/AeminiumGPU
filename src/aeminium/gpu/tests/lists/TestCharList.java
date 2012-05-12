package aeminium.gpu.tests.lists;

import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.PList;

public class TestCharList extends TestAbstractListTemplate<Character> {

	public void testToString() {
		String s = "Hello World!";
		CharList cs = new CharList();
		for (char c : s.toCharArray()) {
			cs.add(c);
		}
		assertEquals(s, cs.toString());
	}

	@Override
	PList<Character> getEmptyList() {
		return new CharList();
	}

	protected Character getSomeValue() {
		return 'c';
	}

	@Override
	Character getSomeOtherValue() {
		return 'k';
	}

	@Override
	PList<Character> createBigList(int size, int delta) {
		char[] memory = new char[size - delta];
		char b = 'd';
		CharList fl = new CharList(memory, size - delta);
		for (int i = size - delta; i < size + delta; i++) {
			fl.add(b);
		}
		return fl;
	}
}
