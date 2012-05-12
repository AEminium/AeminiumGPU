package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class TestRange extends TestCase {

	private static int TEST_SIZE = 10000;

	public void testMapRange() {
		PList<Integer> example = new Range(TEST_SIZE);
		PList<Integer> output = example
				.map(new LambdaMapper<Integer, Integer>() {

					@Override
					public Integer map(Integer input) {
						return 2 * input;
					}

					public String getSource() {
						return "return 2 * input;";
					}

				});

		for (int i = 0; i < TEST_SIZE; i++) {
			assertEquals(2 * i, output.get(i).intValue());
		}
	}

	public void testReduceRange() {
		PList<Integer> example = new Range(10);
		Integer output = example.reduce(new LambdaReducerWithSeed<Integer>() {

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}

		});

		assertEquals(new Integer(45), output);
	}

	public void testMapReduceRange() {
		PList<Integer> example = new Range(10);
		example = example.map(new LambdaMapper<Integer, Integer>() {

			@Override
			public Integer map(Integer input) {
				return 3 * input;
			}

			public String getSource() {
				return "return 3 * input;";
			}

		});

		Integer output = example.reduce(new LambdaReducerWithSeed<Integer>() {

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

		assertEquals(new Integer(3 * 45), output);
	}

}
