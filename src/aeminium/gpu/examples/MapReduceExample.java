package aeminium.gpu.examples;

import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class MapReduceExample {
	public static void main(String[] args) {
		int N = 1034;
		
		PList<Integer> input = new Range(N);
		input = input.map(new LambdaMapper<Integer, Integer>() {

			@Override
			public Integer map(Integer input) {
				return input + 1;
			}
			
			@Override
			public String getSource() {
				return "return input + 1;";
			}
			
		});
		int sum = input.reduce(new LambdaReducerWithSeed<Integer>(){

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
		
		System.out.println("The sum of the first " + N + " numbers is " + sum);
		
		
	}
}
