package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class ReduceExample {
	public static void main(String[] args) {
		int N = 1034;

		PList<Integer> input = new IntList();
		for (int i=0; i<N; i++)
			input.add(i+1);
		
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
		System.out.println("The max value is " + sum + ", and should be " + N);

	}
}
