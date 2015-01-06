package aeminium.gpu.examples;

import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class ReduceExample {
	public static void main(String[] args) {
		int N = 1034;

		PList<Integer> input = new Range(N);
		
		int sum = input.reduce(new LambdaReducerWithSeed<Integer>() {

			@Override
			public Integer combine(Integer input, Integer other) {
				return Math.max(input, other);
			}

			@Override
			public String getSource() {
				return "return max(reduce_input_first, reduce_input_second);";
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

		});
		System.out.println("The max value is " + sum + ", and should be " + (N-1));

	}
}
