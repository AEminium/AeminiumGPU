package aeminium.gpu.examples;

import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class CompareSumSin {
	public static void main(String[] args) {
		int[] tests = new int[]{ 70, 600, 5000, 4000, 30000, 200000, 1000000};
		for (int i : tests) {
			System.out.println("Testing GPU vs CPU for N="+ i);
			runForN(i);
		}
		
		
	}

	private static void runForN(int N) {
		PList<Float> output;
		PList<Float> input = new FloatList();
		for (int i = 0; i < N; i++) {
			input.add((float) i);
		}
		System.out.println("GPU op: unit");
		output = input.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return input + 1;
			}
			
			@Override
			public String getSource() {
				return "return input + 1;";
			}
			
		});
		System.out.println("First el of sum: " + output.get(0));
		System.out.println("GPU op: sin " + input.size());
		output = input.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return (float) Math.sin(input);
			}
			
			@Override
			public String getSource() {
				return "return sin(input);";
			}
			
		});
		System.out.println("First el of sin: " + output.get(0));
	}
}
