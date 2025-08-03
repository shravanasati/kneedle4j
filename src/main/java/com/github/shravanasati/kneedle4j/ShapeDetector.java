package com.github.shravanasati.kneedle4j;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Shape detector utility for detecting direction and curve type of data.
 */
public class ShapeDetector {

	/**
	 * Result class to hold both direction and curve type.
	 */
	public static class ShapeResult {
		private final Enums.DIRECTION direction;
		private final Enums.CURVE_TYPE curveType;

		public ShapeResult(Enums.DIRECTION direction, Enums.CURVE_TYPE curveType) {
			this.direction = direction;
			this.curveType = curveType;
		}

		public Enums.DIRECTION getDirection() {
			return direction;
		}

		public Enums.CURVE_TYPE getCurveType() {
			return curveType;
		}
	}

	/**
	 * Detect the direction and curve type of the line.
	 * 
	 * @param x array of x coordinates
	 * @param y array of y coordinates
	 * @return ShapeResult containing direction ("increasing" or "decreasing")
	 *         and curve type ("concave" or "convex")
	 * @throws IllegalArgumentException if arrays are null, empty, or of different
	 *                                  lengths
	 */
	public static ShapeResult findShape(double[] x, double[] y) {
		if (x == null || y == null) {
			throw new IllegalArgumentException("Input arrays cannot be null");
		}
		if (x.length == 0 || y.length == 0) {
			throw new IllegalArgumentException("Input arrays cannot be empty");
		}
		if (x.length != y.length) {
			throw new IllegalArgumentException("Input arrays must have the same length");
		}
		if (x.length < 2) {
			throw new IllegalArgumentException("Need at least 2 points for shape detection");
		}

		// Perform linear regression using Apache Commons Math
		SimpleRegression regression = new SimpleRegression();
		for (int i = 0; i < x.length; i++) {
			regression.addData(x[i], y[i]);
		}

		double slope = regression.getSlope();
		double intercept = regression.getIntercept();

		// Calculate indices for middle 60% of the data (from 20% to 80%)
		int x1 = (int) (x.length * 0.2);
		int x2 = (int) (x.length * 0.8);

		// Ensure we have valid indices
		if (x1 >= x2) {
			x1 = 0;
			x2 = x.length - 1;
		}

		// Calculate q: mean deviation of actual y values from linear regression in
		// middle section
		double q = calculateMeanDeviation(x, y, x1, x2, slope, intercept);

		// Determine direction and curve type based on slope and q
		if (slope > 0 && q > 0) {
			return new ShapeResult(Enums.DIRECTION.INCREASING, Enums.CURVE_TYPE.CONCAVE);
		}
		if (slope > 0 && q <= 0) {
			return new ShapeResult(Enums.DIRECTION.INCREASING, Enums.CURVE_TYPE.CONVEX);
		}
		if (slope <= 0 && q > 0) {
			return new ShapeResult(Enums.DIRECTION.DECREASING, Enums.CURVE_TYPE.CONCAVE);
		}
		return new ShapeResult(Enums.DIRECTION.DECREASING, Enums.CURVE_TYPE.CONVEX);
	}

	/**
	 * Calculate mean deviation of actual y values from linear regression in
	 * specified range.
	 */
	private static double calculateMeanDeviation(double[] x, double[] y, int x1, int x2,
			double slope, double intercept) {
		double sumActualY = 0;
		double sumPredictedY = 0;
		int count = x2 - x1;

		if (count <= 0) {
			return 0;
		}

		for (int i = x1; i < x2; i++) {
			sumActualY += y[i];
			sumPredictedY += x[i] * slope + intercept;
		}

		double meanActualY = sumActualY / count;
		double meanPredictedY = sumPredictedY / count;

		return meanActualY - meanPredictedY;
	}
}
