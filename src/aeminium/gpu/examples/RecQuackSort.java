package aeminium.gpu.examples;

import java.util.Random;

import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.stacks.Stack2;
import aeminium.gpu.collections.stacks.StackList2;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;

public class RecQuackSort {
	
	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		int n = 10;
		if (args.length > 0) n = Integer.parseInt(args[0]);
		final int size = n;		
	
		final PList<Double> arr = new DoubleList();
		for (int i=0; i<n; i++) {
			arr.add((new Random()).nextDouble());
			System.out.print(arr.get(i) + ", ");
		}
		System.out.println("");
		
		long startTime = System.nanoTime();
		
		BinaryRecursiveStrategy<Integer, Stack2<Integer,Integer>> integral = new BinaryRecursiveStrategy<Integer, Stack2<Integer,Integer>>() {

			public PList<Double> array = arr;
			
			@Override
			public Integer call(Stack2<Integer,Integer> n, RecursiveCallback result) {
				if (n.get2() - n.get1() <= 1) {
					result.markDone();
					return 0;
				}
				System.out.println("left:" + n.get1() + ", right " + n.get2());
				double tmp;
				int left = n.get1();
				int right = n.get2();
				int leftIdx = left;
				int rightIdx = right;
				int pivot = (left + right) / 2;
			    while (leftIdx <= pivot && rightIdx >= pivot){
			      while (array.get(leftIdx) < array.get(pivot) && leftIdx <= pivot){
			        leftIdx++;
			      }
			      while (array.get(rightIdx) > array.get(pivot) && rightIdx >= pivot){
			        rightIdx--;
			      }
			      tmp = array.get(leftIdx);
			      array.set(leftIdx, array.get(rightIdx));
			      array.set(rightIdx, tmp);

			      leftIdx++;
			      rightIdx--;
			      if (leftIdx - 1 == pivot){
			        pivot = ++rightIdx;
			      }
			      else if (rightIdx + 1 == pivot){
			        pivot = --leftIdx;
			      }
			    }
				return pivot;
			}
			
			@Override
			public String getSource() {
				return "if (n <= 2) { *result = 1; return 1; } return 0;"
						+ ""
						+ "current[0]_1 = left; next[0] = n-2;"; 
			}
			
			public String[] getParameters() {
				return new String[] { "n_get1", "n_get2", "result" };
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
			public PList<Stack2<Integer,Integer>> split(Stack2<Integer,Integer> n, Integer pivot) {
				PList<Stack2<Integer,Integer>> l = new StackList2<Integer, Integer>("Integer", "Integer");
				if (pivot == null || pivot == 0) {
					l.add(n);
					return l;
				}
				if (pivot - n.get1() > 0) l.add(new Stack2<Integer,Integer>(n.get1(), pivot-1));
				if (n.get2() - pivot > 0) l.add(new Stack2<Integer,Integer>(pivot+1, n.get2()));
				
				return l;
			}
			
			public String getSplitSource() {
				return "return 0;";
			}

			@Override
			public Stack2<Integer,Integer> getArgument() {
				return new Stack2<Integer, Integer>(0, size);
			}
			
		};
		
		integral.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.print("# R: ");
		for (Double d : arr) {
			System.out.print(d + ", ");
		}
		System.out.println();
		System.out.println(time);

	}
}