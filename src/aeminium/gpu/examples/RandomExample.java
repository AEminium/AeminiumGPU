package aeminium.gpu.examples;

import aeminium.gpu.collections.lazyness.RandomList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class RandomExample {
	public static void main(String[] args) {
		int N = 1034;

		PList<Float> input = new RandomList(N, 123);
		input = input.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return input;
			}

			@Override
			public String getSource() {
				return "return input;";
			}

		});

		for (int i=0; i<20; i++)
			System.out.println("E(" + i + ") = " + input.get(i));
	}
}
