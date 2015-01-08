package aeminium.gpu.examples;

import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.RecursiveCallback;
import aeminium.gpu.operations.functions.RecursiveStrategy;

public class RecIntegral {
	public static void main(String[] args) {
		
		RecursiveStrategy<Double, Double> integral = new RecursiveStrategy<Double, Double>() {

			public Double getStart() { return 0.0; }
			public Double getEnd() { return 1.0; }
			
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
				if (Math.abs(alr - prev) <= 1.0e-8) {
					result.markDone();
				}
				return alr;
			}
			
			@Override
			public String getSource() {
				return "double h = (r - l) * 0.5;\n double c = l + h;\n double fr = (r * r + 1.0) * r;\n double fl = (l * l + 1.0) * l;\n double fc = (c * c + 1.0) * c;\n double hh = h * 0.5;\n double al = (fl + fc) * hh;\n double ar = (fr + fc) * hh;\n double alr = al + ar;\n double prev = (fl+fr) * hh;\n if (fabs(alr - prev) <= 1.0e-8) { result[result_line_id] = 1; }\n return alr;"; 
			}
			
			public String[] getParameters() {
				return new String[] { "r", "l", "result" };
			}
			
			
			@Override
			public Double combine(Double input, Double other) {
				return input+other;
			}
			@Override
			public void split(PList<Double> indices, int index, Double start, Double end, int n) {
				double step = (end-start)/((double)n);
				for (int i=0; i<n; i++) {
					indices.set(index+i, start + i*step); 
				}
			}
			@Override
			public Double getSeed() {
				return 0.0;
			}
			
		};
		
		double value = integral.evaluate();
		System.out.println("Result: " + value);

	}
}
