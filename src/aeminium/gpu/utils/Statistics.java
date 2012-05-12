package aeminium.gpu.utils;

public class Statistics {
	/**
	 * @param population
	 *            an array, the population
	 * @return the variance
	 */
	public static double variance(long[] population) {
		long n = 0;
		double mean = 0;
		double s = 0.0;

		for (double x : population) {
			n++;
			double delta = x - mean;
			mean += delta / n;
			s += delta * (x - mean);
		}
		// if you want to calculate std deviation
		// of a sample change this to (s/(n-1))
		return (s / n);
	}

	/**
	 * @param population
	 *            an array, the population
	 * @return the standard deviation
	 */
	public static double standard_deviation(long[] population) {
		return Math.sqrt(variance(population));
	}
}
