package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class MapToMapExample {
	public static void main(String[] args) {
		int N = 1034;

		PList<Integer> input = new IntList();
		for (int i=0; i<N; i++)
			input.add(i+1);
		
		for (int i = 0; i < 3; i++) {
			input = input.map(new LambdaMapper<Integer, Integer>() {

				@Override
				public Integer map(Integer input) {
					return 2 * input;
				}

				public String getSource() {
					return "return 2 * input;";
				}

			});
		}
		
		System.out.println("The first value is " + input.get(0));

	}
}
