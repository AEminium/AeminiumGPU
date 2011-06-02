package aeminium.gpu.examples;

import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class CompareSumSin {
	public static void main(String[] args) {
	    int size = 10;
	    int inc = 10;
	    while (size < 10000000) {
	        System.out.println("Testing GPU vs GPU for N="+size);
	        runForN(size);
	        size += inc;
	        if (size >= 10*inc) {
	            inc *= 10;
	        }
	    }
	}

	private static void runForN(int N) {
		PList<Float> output;
		PList<Float> input = new FloatList();
		for (int i = 0; i < N; i++) {
			input.add((float) i);
		}
		
		/* UNIT */
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
		
		
		
		/* SIN */
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
		
		
		/* SIN and COS */
		System.out.println("GPU op: sin+cos " + input.size());
		output = input.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return (float) (Math.sin(input) + Math.cos(input));
			}
			
			@Override
			public String getSource() {
				return "return sin(input) + cos(input);";
			}
			
		});
		System.out.println("First el of sin+cos: " + output.get(0));
		
		
		/* Factorial */
		System.out.println("GPU op: factorial " + input.size());
		output = input.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				int r = 1;
				for (int i=1;i<=10000; i++) {
					r *= i;
				}
				return (float) r;
			}
			
			@Override
			public String getSource() {
				return "int r=1; for (int i=1;i<=10000; i++) r *= i; return r;";
			}
			
		});
		System.out.println("First el of factorial: " + output.get(0));
	}
}
