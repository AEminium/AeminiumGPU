package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.stacks.Stack2;
import aeminium.gpu.collections.stacks.StackList2;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;

public class RecNqueens {

	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		int n = 8;
		if (args.length > 0) n = Integer.parseInt(args[0]);
		final int SIZE = n;
				
		long startTime = System.nanoTime();
		
		BinaryRecursiveStrategy<Integer, Stack2<Integer,IntList>> integral = new BinaryRecursiveStrategy<Integer, Stack2<Integer,IntList>>() {

			public int size = SIZE;
			
			@Override
			public Integer call(Stack2<Integer,IntList> stack, RecursiveCallback result) {
				// TODO: Check OK
				if (stack.get1() == SIZE) {
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
			public PList<Stack2<Integer,IntList>> split(Stack2<Integer,IntList> stack) {
				PList<Stack2<Integer,IntList>> l = new StackList2<Integer, IntList>("Integer", "IntList");
				for (int i=0; i < size; i++) {
					Stack2<Integer,IntList> s = stack.copy();
					System.out.println("S: " +s.get2() + ", stack: " + stack.get2());
					System.out.println("---");
					s.get2().set(s.get1(), i);
					s.set1(s.get1()+1);
				}
				return l;
			}
			
			public String getSplitSource() {
				return "if (n <= 2) { return 0; } else { current[0] = n-1; next[0] = n-2; return 1;}";
			}

			@Override
			public Stack2<Integer,IntList> getArgument() {
				return new Stack2<Integer, IntList>(0, new IntList(new int[8], 8));
			}
		};
		
		int value = integral.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.println("# R: " + value);
		System.out.println(time);

	}
}
