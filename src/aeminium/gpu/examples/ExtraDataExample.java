package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

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

	}
}
