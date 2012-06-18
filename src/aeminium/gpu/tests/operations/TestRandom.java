package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.collections.lazyness.RandomList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class TestRandom extends TestCase {

	private static int TEST_SIZE = 10000;

	public void testMapRandom() {
		
		LambdaMapper<Float, Integer> mapper = new LambdaMapper<Float, Integer>() {

			@Override
			public Integer map(Float input) {
				return Math.round(input * 10);
			}

			public String getSource() {
				return "return round(10 * input);";
			}

		};
		
		PList<Integer> output = new RandomList(TEST_SIZE, 123).map(mapper);
		PList<Integer> output2 = new RandomList(TEST_SIZE, 321).map(mapper);

		
		assertEquals(output.size(), TEST_SIZE);
		assertEquals(output2.size(), TEST_SIZE);
		boolean cond = true;
		for (int i = 0; i < TEST_SIZE; i++) {
			cond = cond && (output.get(i).intValue() == output2.get(i).intValue());
		}
		assertTrue(!cond);
	}

}
