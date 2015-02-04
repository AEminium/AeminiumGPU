package aeminium.gpu.examples;

import aeminium.gpu.operations.functions.Range1D;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.gpu.operations.functions.Recursive1DStrategy;

public class RecIntegral {
	
	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		double width = 1.0;
		if (args.length > 0) width = 1.0 * Integer.parseInt(args[0]);
		final double widthi = width;
		
		int depth = 2;
		if (args.length > 1) depth = Integer.parseInt(args[1]);
		final int depthi = depth;
		
		long startTime = System.nanoTime();
		
		Recursive1DStrategy<Double, Double> integral = new Recursive1DStrategy<Double, Double>() {

			public Double getStart() { return 0.0; }
			public Double getEnd() { return widthi; }
			
			@Override
			public Double iterative(Double r, Double l, RecursiveCallback result) {
				double h = (r - l) * 0.5;
				double c = l + h;
				double fr = (r * r + 1.0) * r;
				double fl = (l * l + 1.0) * l;
				double fc = (c * c + 1.0) * c;
				double hh = h * 0.5;
				double al = (fl + fc) * hh;
				double ar = (fr + fc) * hh;
				double alr = al + ar;
				double prev = (fl+fr) * hh;
				if (Math.abs(alr - prev) <= Math.pow(10,-depthi)) {
					result.markDone();
				}
				return alr;
			}
			
			@Override
			public String getSource() {
				return "double h, hh, c, fr, fl, fc, ar, al, alr, prev;\n h = (r - l) * 0.5;\n c = l + h;\n fr = (r * r + 1.0) * r;\n fl = (l * l + 1.0) * l;\n fc = (c * c + 1.0) * c;\n hh = h * 0.5;\n al = (fl + fc) * hh;\n ar = (fr + fc) * hh;\n alr = al + ar;\n prev = (fl+fr) * hh;\n if (fabs(alr - prev) <= 1.0e-" + depthi + ") { result[0] = 1; }\n return alr;"; 
			}
			
			public String[] getParameters() {
				return new String[] { "r", "l", "__t", "__b", "result" };
			}
			
			
			@Override
			public Double combine(Double input, Double other) {
				return input+other;
			}
			
			@Override
			public String getCombineSource() {
				return "return first + second;";
			}
			
			public String[] getCombineParameters() {
				return new String[] { "first", "second" };
			}
			
			@Override
			public Range2D<Double, Void> split(Double start, Double end, int n) {
				double step = (end-start)/((double) n);
				Range1D<Double> r = new Range1D<Double>("Double");
				while (start < end) {
					r.add(start, start+step);
					start = start+step;
				}
				return r;
			}
			@Override
			public String getSplitSource() {
				return "stepX = stepX/2;";
			}
			
			@Override
			public Double getSeed() {
				return 0.0;
			}
			
		};
		
		double value = integral.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.println("# R: " + value);
		System.out.println(time);

	}
}
