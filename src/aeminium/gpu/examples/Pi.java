package aeminium.gpu.examples;

import aeminium.gpu.collections.lazyness.RandomList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.functions.LambdaReducerWithSeed;

public class Pi {
	public static void main(String[] args) {
		int RESOLUTION = 100000;
		RandomList rl = new RandomList((RESOLUTION * 2), 12345);
		PMatrix<java.lang.Float> m = rl.groupBy(2);
		PList<java.lang.Float> pair = m
				.reduceLines(new LambdaReducer<java.lang.Float>() {
					@Override
					public Float combine(Float input, Float other) {
						return (java.lang.Math.sqrt(((java.lang.Math.pow(input,
								2)) + (java.lang.Math.pow(other, 2))))) <= 1 ? 1.0F
								: 0.0F;
					}

					public String getSource() {
						return "{\nreturn (sqrt(((pow((double) input, (double) 2)) + (pow((double) other, (double) 2))))) <= 1 ? 1.0 : 0.0;\n}";
					}

					public String getId() {
						return "reducecode_PiCalc34";
					}

					public String[] getParameters() {
						return new String[] { "input", "other" };
					}

					public String getSourceComplexity() {
						return "1*sqrt+2*pow+2*plus";
					}

				});
		Float f = pair.reduce(new LambdaReducerWithSeed<java.lang.Float>() {
			public String getSeedSource() {
				return "{\nreturn ((float)(0));\n}";
			}

			@Override
			public Float combine(Float input, Float output) {
				return input + output;
			}

			public String getSource() {
				return "{\nreturn input + output;\n}";
			}

			@Override
			public Float getSeed() {
				return ((float) (0));
			}

			public String getId() {
				return "reducecode_PiCalc35";
			}

			public String[] getParameters() {
				return new String[] { "input", "output" };
			}

			public String getSourceComplexity() {
				return "1*plus";
			}

		});
		float pi = (4 * f) / ((float) (RESOLUTION));
		System.out.println(("PI: " + pi));
	}

}
