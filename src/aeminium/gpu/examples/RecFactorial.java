package aeminium.gpu.examples;

import aeminium.gpu.operations.functions.Range1D;
import aeminium.gpu.operations.functions.Recursive1DStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;

public class RecFactorial {


	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		int width = 100000;
		if (args.length > 0) width = Integer.parseInt(args[0]);
		final int widthi = width;
		
		long startTime = System.nanoTime();
		
		Recursive1DStrategy<Integer, Long> integral = new Recursive1DStrategy<Integer, Long>() {

			public Integer getStart() { return 1; }
			public Integer getEnd() { return widthi; }
			
			@Override
			public Long iterative(Integer l, Integer r, RecursiveCallback result) {
				if (r - l < 10) {
					long acc = 1;
					for (int i=l;i<r;i++)
						acc *= i;
					result.markDone();
					return acc;
				}
				return 1L;
			}
			
			@Override
			public String getSource() {
				return "if (r - l < 10) { long acc = 1; for (int i=l;i<r;i++) { acc *= i }"
						+ "*result = 1; return acc }\n return 1;"; 
			}
			
			public String[] getParameters() {
				return new String[] { "l", "r", "__t", "__b", "result" };
			}
			
			
			@Override
			public Long combine(Long input, Long other) {
				return input*other;
			}
			
			@Override
			public String getCombineSource() {
				return "return first * second;";
			}
			
			public String[] getCombineParameters() {
				return new String[] { "first", "second" };
			}
			
			@Override
			public Range1D<Integer> split(Integer start, Integer end, int n) {
				Range1D<Integer> r = new Range1D<Integer>("Integer");
				 
				int step = (end-start)/n;
				if (step <= 0) return r;
				while (start < end) {
					if (r.size() == n-1) {
						r.add(start, end);
						break;
					} else {
						r.add(start, start+step);
						start = start+step;
					}
				}
				return r;
			}
			@Override
			public String getSplitSource() {
				return "stepX = stepX/2;";
			}
			
			@Override
			public Long getSeed() {
				return 1L;
			}
			
		};
		
		double value = integral.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.println("# R: " + value);
		System.out.println(time);

	}
}
