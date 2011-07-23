package aeminium.gpu.tests.lists;

import aeminium.gpu.collections.lists.PList;
import junit.framework.TestCase;

public abstract class TestAbstractListTemplate<T> extends TestCase {
	
	protected int MAX_SIZE = 10000000;
	
	abstract PList<T> getEmptyList();

	abstract T getSomeValue();
	abstract T getSomeOtherValue();
	abstract PList<T> createBigList(int size, int delta);
	
	public void testGeneralFloatListOperations() {
		
		T f1 = getSomeValue();
		T f2 = getSomeOtherValue();
		
		PList<T> fl = getEmptyList();
		assertEquals(true, fl.isEmpty());
		
		fl.add(getSomeValue());
		assertEquals(1, fl.size());
		assertEquals(1, fl.length());
		assertEquals(false, fl.isEmpty());
		
		fl.add(f2);
		assertEquals(2, fl.size());
		assertEquals(false, fl.isEmpty());
		
		assertEquals(f2,fl.get(1));
		assertEquals(f1, fl.get(0));
		
		fl.set(0, f2);
		assertEquals(f2, fl.get(0));

		fl.set(2, f2);
		assertEquals(3, fl.size());
		
		fl.remove(0);
		assertEquals(2, fl.size());
		assertEquals(false, fl.isEmpty());
		
		fl.remove(f2);
		assertEquals(0, fl.size());
		assertEquals(true, fl.isEmpty());
	}
	
	public void testLimits() {
		PList<T> fl = createBigList(MAX_SIZE,50);
		assertEquals(MAX_SIZE + 50, fl.size());
	}

	
	
}
