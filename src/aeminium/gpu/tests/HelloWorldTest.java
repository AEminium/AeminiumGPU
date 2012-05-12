package aeminium.gpu.tests;

import junit.framework.TestCase;
import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.PList;

public class HelloWorldTest extends TestCase {

	protected float FLOAT_PRECISION = 0.00001f;

	public void testMap() {
		PList<Float> example = new FloatList();
		Float f = new Float(1.1);
		example.add(f);
		assertEquals(1.1, example.get(0).floatValue(), FLOAT_PRECISION);
	}

}
