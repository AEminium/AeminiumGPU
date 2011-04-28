package aeminium.gpu.examples;

import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public class MapReduceExample {
	public static void main(String[] args) {
		int N = 512;
		
		PList<Integer> input = new IntList();
		for (int i = 0; i < N; i++) {
			input.add(i);
		}
		
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
		input.get(0);
		int sum = input.reduce(new LambdaReducer<Integer>(){

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
