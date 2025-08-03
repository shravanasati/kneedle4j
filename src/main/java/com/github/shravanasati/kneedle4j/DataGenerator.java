package com.github.shravanasati.kneedle4j;

import java.util.Random;
import java.util.Arrays;

/**
 * Generate synthetic data to work with kneed.
 */
public class DataGenerator {

	/**
	 * Result class to hold x and y arrays.
	 */
	public static class DataPair {
		private final double[] x;
		private final double[] y;

		public DataPair(double[] x, double[] y) {
			this.x = x;
			this.y = y;
		}

		public double[] getX() {
			return x;
		}

		public double[] getY() {
			return y;
		}
	}

	/**
	 * Recreate NoisyGaussian from the original kneedle paper.
	 *
	 * @param mu    The mean value to build a normal distribution around
	 * @param sigma The standard deviation of the distribution
	 * @param N     The number of samples to draw from to build the normal
	 *              distribution
	 * @param seed  An integer to set the random seed
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair noisyGaussian(double mu, double sigma, int N, long seed) {
		Random random = new Random(seed);
		double[] z = new double[N];

		// Generate normal distribution
		for (int i = 0; i < N; i++) {
			z[i] = random.nextGaussian() * sigma + mu;
		}

		// Sort the array
		Arrays.sort(z);

		// Create y values as normalized range
		double[] y = new double[N];
		for (int i = 0; i < N; i++) {
			y[i] = (double) i / N;
		}

		return new DataPair(z, y);
	}

	/**
	 * Recreate NoisyGaussian with default parameters.
	 */
	public static DataPair noisyGaussian() {
		return noisyGaussian(50.0, 10.0, 100, 42L);
	}

	/**
	 * Recreate the values in figure 2 from the original kneedle paper.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair figure2() {
		double[] x = linspace(0.0, 1.0, 10);
		double[] y = new double[x.length];

		for (int i = 0; i < x.length; i++) {
			y[i] = -1.0 / (x[i] + 0.1) + 5.0;
		}

		return new DataPair(x, y);
	}

	/**
	 * Generate a sample increasing convex function.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair convexIncreasing() {
		double[] x = arange(0, 10);
		double[] y = { 1, 2, 3, 4, 5, 10, 15, 20, 40, 100 };

		return new DataPair(x, y);
	}

	/**
	 * Generate a sample decreasing convex function.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair convexDecreasing() {
		double[] x = arange(0, 10);
		double[] y = { 100, 40, 20, 15, 10, 5, 4, 3, 2, 1 };

		return new DataPair(x, y);
	}

	/**
	 * Generate a sample decreasing concave function.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair concaveDecreasing() {
		double[] x = arange(0, 10);
		double[] y = { 99, 98, 97, 96, 95, 90, 85, 80, 60, 0 };

		return new DataPair(x, y);
	}

	/**
	 * Generate a sample increasing concave function.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair concaveIncreasing() {
		double[] x = arange(0, 10);
		double[] y = { 0, 60, 80, 85, 90, 95, 96, 97, 98, 99 };

		return new DataPair(x, y);
	}

	/**
	 * Generate a sample function with local minima/maxima.
	 *
	 * @return DataPair containing x and y arrays
	 */
	public static DataPair bumpy() {
		double[] x = arange(0, 90);
		double[] y = {
				7305.0, 6979.0, 6666.6, 6463.2, 6326.5, 6048.8, 6032.8, 5762.0, 5742.8, 5398.2,
				5256.8, 5227.0, 5001.7, 4942.0, 4854.2, 4734.6, 4558.7, 4491.1, 4411.6, 4333.0,
				4234.6, 4139.1, 4056.8, 4022.5, 3868.0, 3808.3, 3745.3, 3692.3, 3645.6, 3618.3,
				3574.3, 3504.3, 3452.4, 3401.2, 3382.4, 3340.7, 3301.1, 3247.6, 3190.3, 3180.0,
				3154.2, 3089.5, 3045.6, 2989.0, 2993.6, 2941.3, 2875.6, 2866.3, 2834.1, 2785.1,
				2759.7, 2763.2, 2720.1, 2660.1, 2690.2, 2635.7, 2632.9, 2574.6, 2556.0, 2545.7,
				2513.4, 2491.6, 2496.0, 2466.5, 2442.7, 2420.5, 2381.5, 2388.1, 2340.6, 2335.0,
				2318.9, 2319.0, 2308.2, 2262.2, 2235.8, 2259.3, 2221.0, 2202.7, 2184.3, 2170.1,
				2160.0, 2127.7, 2134.7, 2102.0, 2101.4, 2066.4, 2074.3, 2063.7, 2048.1, 2031.9
		};

		return new DataPair(x, y);
	}

	/**
	 * Create an array of linearly spaced values.
	 * Equivalent to numpy's linspace function.
	 *
	 * @param start The starting value
	 * @param stop  The ending value (inclusive)
	 * @param num   The number of samples to generate
	 * @return Array of linearly spaced values
	 */
	private static double[] linspace(double start, double stop, int num) {
		if (num <= 0) {
			return new double[0];
		}
		if (num == 1) {
			return new double[] { start };
		}

		double[] result = new double[num];
		double step = (stop - start) / (num - 1);

		for (int i = 0; i < num; i++) {
			result[i] = start + i * step;
		}

		return result;
	}

	/**
	 * Create an array of integers from start to stop (exclusive).
	 * Equivalent to numpy's arange function.
	 *
	 * @param start The starting value (inclusive)
	 * @param stop  The ending value (exclusive)
	 * @return Array of integer values as doubles
	 */
	private static double[] arange(int start, int stop) {
		int size = stop - start;
		double[] result = new double[size];

		for (int i = 0; i < size; i++) {
			result[i] = start + i;
		}

		return result;
	}
}
