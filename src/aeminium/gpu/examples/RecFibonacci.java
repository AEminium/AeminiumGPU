package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;

public class RecFibonacci {
	
	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		int n = 8;
		if (args.length > 0) n = Integer.parseInt(args[0]);
		final int fibn = n;
				
		long startTime = System.nanoTime();
		
		BinaryRecursiveStrategy<Integer, Integer> integral = new BinaryRecursiveStrategy<Integer, Integer>() {

			
			@Override
			public Integer call(Integer n, RecursiveCallback result) {
				if (n <= 2) {
					result.markDone();
					return 1;
				} 
				return 0;
			}
			
			@Override
			public String getSource() {
				return "if (n <= 2) { *result = 1; return 1; } return 0;"; 
			}
			
			public String[] getParameters() {
				return new String[] { "n", "result" };
			}
			
			@Override
			public String getCombineSource() {
				return "return first + second;";
			}
			
			public String[] getCombineParameters() {
				return new String[] { "first", "second" };
			}
			
			
			@Override
			public Integer combine(Integer input, Integer other) {
				return input+other;
			}
			
			@Override
			public Integer getSeed() {
				return 0;
			}
			
			@Override
			public PList<Integer> split(Integer n, Integer acc) {
				PList<Integer> l = new IntList();
				if (n <= 2) {
					l.add(n);
				} else {
					l.add(n-1);
					l.add(n-2);
				}
				return l;
			}
			
			public String getSplitSource() {
				return "if (n <= 2) { return 0; } else { current[0] = n-1; next[0] = n-2; return 1;}";
			}

			@Override
			public Integer getArgument() {
				return fibn;
			}
			
		};
		
		int value = integral.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.println("# R: " + value);
		System.out.println(time);

	}
}
