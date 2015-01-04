package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class TestPartialReduce extends TestCase {

	private static int TEST_SIZE = 4*6*12;

	public void testSumReduce() {
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(1);
		}

		Integer output = example.groupBy(512).reduceLines(new LambdaReducer<Integer>(){
			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}
		})  .reduce(new LambdaReducerWithSeed<Integer>() {

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

		});
		assertEquals(TEST_SIZE, output.intValue());
	}

}
