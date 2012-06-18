package aeminium.gpu.tests.operations;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.IntMatrix;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;
import junit.framework.TestCase;

public class TestMatrix extends TestCase {

	private static int TEST_SIZE = 100;
	private static int TEST_SIZE2 = 200;

	public void testMapMatrix() {
		PMatrix<Integer> m = new IntMatrix(TEST_SIZE, TEST_SIZE2);
		
		for (int i = 0; i < TEST_SIZE; i++) {
			for (int j = 0; j < TEST_SIZE2; j++) {
				m.set(i, j, 0);
			}
		}
		
		PMatrix<Integer> m2 = m.map(new LambdaMapper<Integer, Integer>() {

			@Override
			public Integer map(Integer input) {
				return input + 1;
			}

			public String getSource() {
				return "return input+1;";
			}
		});
		
		for (int i = 0; i < TEST_SIZE; i++) {
			for (int j = 0; j < TEST_SIZE2; j++) {
				assertEquals(1, m.get(i, j).intValue());
			}
		}
		
		int i = m.reduce(new LambdaReducerWithSeed<Integer>() {

			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public Integer getSeed() {
				return 0;
			}
		});
		
		//assertEquals(TEST_SIZE * TEST_SIZE2, i);
		
		
		PList<Integer> li = m2.reduceLines(new LambdaReducerWithSeed<Integer>() {

			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

		});
		
		for (int c = 0; c < li.length(); c++) {
			assertEquals(TEST_SIZE, li.get(c).intValue());
		}
		
		
	}

}
