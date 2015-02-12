package aeminium.gpu.examples;

import aeminium.gpu.collections.matrices.DoubleMatrix;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.operations.functions.Range1D;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.operations.functions.Recursive1DStrategy;
import aeminium.gpu.operations.functions.RecursiveCallback;

public class RecHeat {
	
	private static final int COLUMNS = 10;
	private static final long NPS = (1000L * 1000 * 1000);
	
	public static void main(String[] args) {
		int width = 10;
		if (args.length > 0) width = Integer.parseInt(args[0]);
		final int widthi = width;
		
		long startTime = System.nanoTime();
		
		final PMatrix<Double> m = new DoubleMatrix(width, COLUMNS);
		final PMatrix<Double> n = new DoubleMatrix(width, COLUMNS);
		
		fill(m, width);
		
		Recursive1DStrategy<Integer, Double> heat = new Recursive1DStrategy<Integer, Double>() {

			public Integer getStart() { return 0; }
			public Integer getEnd() { return widthi; }
			
			public PMatrix<Double> old = m;
			public PMatrix<Double> newM = n;
			public int size = widthi;
			public double dx = 1.570796326794896558;
			
			@Override
			public Double iterative(Integer r, Integer l, RecursiveCallback result) {
				if (l - r > 1) return 0.0;
				int lb = (r <= 0) ? 1 : r;
				int ub = (l >= size-1) ? size-2 : l;
				for (int a = lb; a < ub; a++) {
					for (int b = 1; b < COLUMNS-1; b++) {
						double val = 1/dx * (old.get(a+1, b) - 2 * old.get(a,b) + old.get(a-1,b)) + 1/dx * ( old.get(a, b+1) - 2 * old.get(a, b) + old.get(a, b-1)) + old.get(a, b);
						newM.set(a, b, val);
					}
				}
				result.markDone();
				return 1.0 * (l-r);
			}
			
			@Override
			public String getSource() {
				return "" // if (l - r > 1) return 0.0;
						+ "int lb = (r<=0) ? 1 : r;"
						+ "int ub = (l>=size-1) ? size -2 : l;"
						+ "for (int a = lb; a<ub; a++) {"
						+ "for (int b=1; b<"+COLUMNS+"-1; b++) {"
						+ "double val = 1/dx * ( old[(a+1) * __old_cols + b] - 2 * old[a * __old_cols + b] + old[(a-1) * __old_cols + b]) + "
						+ "1/dx * ( old[a * __old_cols + (b+1)] - 2 * old[a * __old_cols + b] + old[a * __old_cols + (b-1)]) + old[a * __old_cols + b];"
						+ "newM[a * __newM_cols + b] = val;"
						+ "}"
						+ "}"
						+ "*result = 1;"
						+ "return l-r;"; 
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
			public Range2D<Integer, Void> split(Integer start, Integer end, int n) {
				int step = (end-start)/n;
				if (step == 0) step = 1;
				Range1D<Integer> r = new Range1D<Integer>("Integer");
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
				return "stepX = stepX/2; if (stepX < 1) stepX=1;";
			}
			
			@Override
			public Double getSeed() {
				return 0.0;
			}
			
		};
		
		double val = heat.evaluate();
		double time = (System.nanoTime() - startTime) * 1.0 / NPS;
		System.out.println("# R: " + val + " : " + n.get(5, 4));
		System.out.println(time);

	}

	public static double f(double x, double y) {
		return Math.sin(x) * Math.sin(y);
	}
	
	public static double randa(double x, double t) {
		return 0.0;
	}
	
	public static double randb(double x, double t) {
		return Math.exp(-2*(t))*Math.sin(x);
	}
	
	public static double randc(double y, double t) {
		return 0.0;
	}
	
	public static double randd(double y, double t) {
		return Math.exp(-2*(t))*Math.sin(y);
	}
	
	private static void fill(PMatrix<Double> m, int size) {
		double dx = 1.570796326794896558;
		for (int a=0; a < size; a++) {
			for (int b=0; b < COLUMNS; b++) {
				if (a == 0) {
					m.set(a, b, randa(a * dx, 0));
				} else if (a == size-1) {
					m.set(a, b, randd(b * dx, 0));
				} else if (b == 0) {
					m.set(a, b, randd(a * dx, 0));
				} else if (b == size-1) {
					m.set(a, b, randc(b * dx, 0));
				} else {
					m.set(a, b, f(a*dx, b*dx));
				}
			}
		}
	}
}
