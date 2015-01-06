package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class ExtraDataExample {
	public static void main(String[] args) {
		int N = 1034;

		PList<Integer> input = new IntList();
		final PList<Integer> input2 = new IntList();
		for (int i=0; i<N; i++) {
			input.add(i);
			input2.add(-i);
		}
		input = input.map(new LambdaMapper<Integer, Integer>() {

			public PList<Integer> m = input2;

			public Integer map(Integer i) {
				return m.get(i);
			}
			
			@Override
			public String getSource() {
				return "return m[input];";
			}
			
			public String getOutputType() {
				return "Integer";
			}

		});
		System.out.println("The first value is " + input.get(N-1));
		
		int sum = input.reduce(new LambdaReducerWithSeed<Integer>() {

			public PList<Integer> m3 = input2;
			
			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other + m3.get(0);
			}

			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second + m3[0];";
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

		});

		System.out.println("The sum of the first " + N + " numbers is " + sum);

		sum = input.map(new LambdaMapper<Integer, Integer>() {

			public PList<Integer> m1 = input2;
			
			@Override
			public Integer map(Integer input) {
				return m1.get(input);
			}

			@Override
			public String getSource() {
				return "return m1[input];";
			}

		}).reduce(new LambdaReducerWithSeed<Integer>() {

			public PList<Integer> m5 = input2;
			
			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other + m5.get(0);
			}

			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second + m5[0];";
			}

			@Override
			public Integer getSeed() {
				return 0;
			}

		});

		System.out.println("The sum of the first " + N + " numbers is " + sum);
		
	}
}
