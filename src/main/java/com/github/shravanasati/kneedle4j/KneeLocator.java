package com.github.shravanasati.kneedle4j;

import java.util.*;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Once instantiated, this class attempts to find the point of maximum
 * curvature on a line. The knee is accessible via the `.getKnee()` method.
 */
public class KneeLocator {

	// Instance variables
	private final double[] x;
	private final double[] y;
	private final Enums.CURVE_TYPE curve;
	private final Enums.DIRECTION direction;
	private final int N;
	private final double S;
	private final boolean online;
	private final int polynomialDegree;
	private final Enums.INTERPOLATION_METHOD interpMethod;

	// Computed values
	private double[] dsY;
	private double[] xNormalized;
	private double[] yNormalized;
	private double[] xDifference;
	private double[] yDifference;
	private int[] maximaIndices;
	private double[] xDifferenceMaxima;
	private double[] yDifferenceMaxima;
	private int[] minimaIndices;
	private double[] xDifferenceMinima;
	private double[] yDifferenceMinima;
	private double[] tmx;

	// Results
	private Double knee;
	private Double normKnee;
	private Double kneeY;
	private Double normKneeY;
	private final Set<Double> allKnees;
	private final Set<Double> allNormKnees;
	private final List<Double> allKneesY;
	private final List<Double> allNormKneesY;

	/**
	 * Constructor for KneeLocator
	 *
	 * @param x                x values, must be the same length as y
	 * @param y                y values, must be the same length as x
	 * @param S                Sensitivity, the minimum number of data points below
	 *                         the local distance maximum before calling a knee
	 * @param curve            If CONCAVE, algorithm will detect knees. If CONVEX,
	 *                         it will detect elbows
	 * @param direction        one of {INCREASING, DECREASING}
	 * @param interpMethod     one of {INTERP1D, POLYNOMIAL}
	 * @param online           kneed will correct old knee points if true, will
	 *                         return first knee if false
	 * @param polynomialDegree The degree of the fitting polynomial. Only used when
	 *                         interpMethod=POLYNOMIAL
	 */
	public KneeLocator(double[] x, double[] y, double S, Enums.CURVE_TYPE curve, Enums.DIRECTION direction,
			Enums.INTERPOLATION_METHOD interpMethod, boolean online, int polynomialDegree) {

		// Validate input
		if (x == null || y == null) {
			throw new IllegalArgumentException("x and y arrays cannot be null");
		}
		if (x.length != y.length) {
			throw new IllegalArgumentException("x and y arrays must have the same length");
		}
		if (x.length < 2) {
			throw new IllegalArgumentException("Need at least 2 data points");
		}
		if (curve == null) {
			throw new IllegalArgumentException("curve cannot be null");
		}
		if (direction == null) {
			throw new IllegalArgumentException("direction cannot be null");
		}

		// Step 0: Raw Input
		this.x = Arrays.copyOf(x, x.length);
		this.y = Arrays.copyOf(y, y.length);
		this.curve = curve;
		this.direction = direction;
		this.N = x.length;
		this.S = S;
		this.online = online;
		this.polynomialDegree = polynomialDegree;
		this.interpMethod = interpMethod;

		this.allKnees = new HashSet<>();
		this.allNormKnees = new HashSet<>();
		this.allKneesY = new ArrayList<>();
		this.allNormKneesY = new ArrayList<>();

		// Step 1: fit a smooth line
		if (interpMethod == Enums.INTERPOLATION_METHOD.INTERP1D) {
			// Use spline interpolation with Apache Commons Math
			this.dsY = splineInterpolation(this.x, this.y);
		} else if (interpMethod == Enums.INTERPOLATION_METHOD.POLYNOMIAL) {
			this.dsY = polynomialFit(x, y, polynomialDegree);
		} else {
			throw new IllegalArgumentException("interpMethod must be INTERP1D or POLYNOMIAL");
		}

		// Step 2: normalize values
		this.xNormalized = normalize(this.x);
		this.yNormalized = normalize(this.dsY);

		// Step 3: Calculate the Difference curve
		this.yNormalized = transformY(this.yNormalized, direction, curve);
		this.yDifference = new double[this.yNormalized.length];
		for (int i = 0; i < this.yNormalized.length; i++) {
			this.yDifference[i] = this.yNormalized[i] - this.xNormalized[i];
		}
		this.xDifference = Arrays.copyOf(this.xNormalized, this.xNormalized.length);

		// Step 4: Identify local maxima/minima
		this.maximaIndices = findLocalMaxima(this.yDifference);
		this.xDifferenceMaxima = getSubArray(this.xDifference, this.maximaIndices);
		this.yDifferenceMaxima = getSubArray(this.yDifference, this.maximaIndices);

		this.minimaIndices = findLocalMinima(this.yDifference);
		this.xDifferenceMinima = getSubArray(this.xDifference, this.minimaIndices);
		this.yDifferenceMinima = getSubArray(this.yDifference, this.minimaIndices);

		// Step 5: Calculate thresholds
		double meanDiff = calculateMeanAbsDiff(this.xNormalized);
		this.tmx = new double[this.yDifferenceMaxima.length];
		for (int i = 0; i < this.yDifferenceMaxima.length; i++) {
			this.tmx[i] = this.yDifferenceMaxima[i] - (this.S * meanDiff);
		}

		// Step 6: find knee
		findKnee();

		// Step 7: If we have a knee, extract data about it
		if (this.knee != null) {
			this.kneeY = findYForX(this.x, this.y, this.knee);
			this.normKneeY = findYForX(this.xNormalized, this.yNormalized, this.normKnee);
		}
	}

	/**
	 * Constructor with default parameters
	 */
	public KneeLocator(double[] x, double[] y) {
		this(x, y, 1.0, Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D,
				false, 7);
	}

	/**
	 * Constructor with curve and direction parameters
	 */
	public KneeLocator(double[] x, double[] y, Enums.CURVE_TYPE curve, Enums.DIRECTION direction) {
		this(x, y, 1.0, curve, direction, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);
	}

	/**
	 * Normalize an array to [0, 1] range
	 */
	private static double[] normalize(double[] a) {
		double min = Arrays.stream(a).min().orElse(0.0);
		double max = Arrays.stream(a).max().orElse(1.0);
		double range = max - min;

		if (range == 0) {
			return new double[a.length]; // All zeros if no variation
		}

		double[] result = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = (a[i] - min) / range;
		}
		return result;
	}

	/**
	 * Transform y to concave, increasing based on given direction and curve
	 */
	private static double[] transformY(double[] y, Enums.DIRECTION direction, Enums.CURVE_TYPE curve) {
		double[] result = Arrays.copyOf(y, y.length);
		double max = Arrays.stream(y).max().orElse(0.0);

		if (direction == Enums.DIRECTION.DECREASING) {
			if (curve == Enums.CURVE_TYPE.CONCAVE) {
				// Flip array
				for (int i = 0; i < result.length / 2; i++) {
					double temp = result[i];
					result[i] = result[result.length - 1 - i];
					result[result.length - 1 - i] = temp;
				}
			} else if (curve == Enums.CURVE_TYPE.CONVEX) {
				// y = max - y
				for (int i = 0; i < result.length; i++) {
					result[i] = max - result[i];
				}
			}
		} else if (direction == Enums.DIRECTION.INCREASING && curve == Enums.CURVE_TYPE.CONVEX) {
			// Flip(max - y)
			for (int i = 0; i < result.length; i++) {
				result[i] = max - result[i];
			}
			// Then flip the array
			for (int i = 0; i < result.length / 2; i++) {
				double temp = result[i];
				result[i] = result[result.length - 1 - i];
				result[result.length - 1 - i] = temp;
			}
		}

		return result;
	}

	/**
	 * Spline interpolation using Apache Commons Math
	 */
	private double[] splineInterpolation(double[] x, double[] y) {
		try {
			SplineInterpolator interpolator = new SplineInterpolator();
			UnivariateFunction function = interpolator.interpolate(x, y);

			double[] result = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				result[i] = function.value(x[i]);
			}
			return result;
		} catch (Exception e) {
			// Fallback to original values if interpolation fails
			return Arrays.copyOf(y, y.length);
		}
	}

	/**
	 * Polynomial fitting using Apache Commons Math
	 */
	private double[] polynomialFit(double[] x, double[] y, int degree) {
		try {
			// Create weighted observed points
			WeightedObservedPoints points = new WeightedObservedPoints();
			for (int i = 0; i < x.length; i++) {
				points.add(x[i], y[i]);
			}

			// Fit polynomial
			PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
			double[] coefficients = fitter.fit(points.toList());

			// Create polynomial function and evaluate at x points
			PolynomialFunction polynomial = new PolynomialFunction(coefficients);
			double[] result = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				result[i] = polynomial.value(x[i]);
			}

			return result;
		} catch (Exception e) {
			// Fallback to linear fitting if polynomial fitting fails
			return linearFit(x, y);
		}
	}

	/**
	 * Linear least squares fitting
	 */
	private double[] linearFit(double[] x, double[] y) {
		int n = x.length;
		double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

		for (int i = 0; i < n; i++) {
			sumX += x[i];
			sumY += y[i];
			sumXY += x[i] * y[i];
			sumXX += x[i] * x[i];
		}

		double denominator = n * sumXX - sumX * sumX;
		if (Math.abs(denominator) < 1e-10) {
			return Arrays.copyOf(y, y.length);
		}

		double slope = (n * sumXY - sumX * sumY) / denominator;
		double intercept = (sumY - slope * sumX) / n;

		double[] result = new double[n];
		for (int i = 0; i < n; i++) {
			result[i] = slope * x[i] + intercept;
		}

		return result;
	}

	/**
	 * Find local maxima in an array
	 */
	private int[] findLocalMaxima(double[] array) {
		List<Integer> maxima = new ArrayList<>();

		for (int i = 1; i < array.length - 1; i++) {
			if (array[i] >= array[i - 1] && array[i] >= array[i + 1]) {
				maxima.add(i);
			}
		}

		// Add endpoints if they are maxima
		if (array.length > 1) {
			if (array[0] >= array[1]) {
				maxima.add(0, 0);
			}
			if (array[array.length - 1] >= array[array.length - 2]) {
				maxima.add(array.length - 1);
			}
		}

		return maxima.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Find local minima in an array
	 */
	private int[] findLocalMinima(double[] array) {
		List<Integer> minima = new ArrayList<>();

		for (int i = 1; i < array.length - 1; i++) {
			if (array[i] <= array[i - 1] && array[i] <= array[i + 1]) {
				minima.add(i);
			}
		}

		// Add endpoints if they are minima
		if (array.length > 1) {
			if (array[0] <= array[1]) {
				minima.add(0, 0);
			}
			if (array[array.length - 1] <= array[array.length - 2]) {
				minima.add(array.length - 1);
			}
		}

		return minima.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Get sub-array based on indices
	 */
	private double[] getSubArray(double[] array, int[] indices) {
		double[] result = new double[indices.length];
		for (int i = 0; i < indices.length; i++) {
			result[i] = array[indices[i]];
		}
		return result;
	}

	/**
	 * Calculate mean absolute difference
	 */
	private double calculateMeanAbsDiff(double[] array) {
		if (array.length < 2)
			return 0.0;

		double sum = 0.0;
		for (int i = 1; i < array.length; i++) {
			sum += Math.abs(array[i] - array[i - 1]);
		}
		return sum / (array.length - 1);
	}

	/**
	 * Find knee point
	 */
	private void findKnee() {
		if (maximaIndices.length == 0) {
			this.knee = null;
			this.normKnee = null;
			return;
		}

		// Check if this is essentially a flat line (no significant variation)
		double maxYDiff = Double.NEGATIVE_INFINITY;
		for (double diff : yDifference) {
			maxYDiff = Math.max(maxYDiff, diff);
		}

		// If the maximum Y difference is too small or negative, no meaningful knee
		// exists
		if (maxYDiff <= 1e-6) {
			this.knee = null;
			this.normKnee = null;
			return;
		}

		int maximaThresholdIndex = 0;
		double currentThreshold = 0.0;
		int currentThresholdIndex = -1;

		for (int i = 0; i < xDifference.length; i++) {
			// Skip points before the first local maxima
			if (i < maximaIndices[0]) {
				continue;
			}

			int j = i + 1;

			// Reached the end of the curve
			if (i == xDifference.length - 1) {
				break;
			}

			// If we're at a local max, set the threshold for subsequent points
			if (contains(maximaIndices, i)) {
				if (maximaThresholdIndex < tmx.length) {
					currentThreshold = tmx[maximaThresholdIndex];
					currentThresholdIndex = i;
					maximaThresholdIndex++;
				}
			}

			// Values in difference curve are at or after a local minimum
			if (contains(minimaIndices, i)) {
				currentThreshold = 0.0;
				currentThresholdIndex = -1;
			}

			if (yDifference[j] < currentThreshold && currentThresholdIndex != -1) {
				Double kneeCandidate = null;
				Double normKneeCandidate = null;

				if (curve == Enums.CURVE_TYPE.CONVEX) {
					if (direction == Enums.DIRECTION.DECREASING) {
						kneeCandidate = x[currentThresholdIndex];
						normKneeCandidate = xNormalized[currentThresholdIndex];
					} else {
						int index = x.length - currentThresholdIndex - 1;
						if (index >= 0 && index < x.length) {
							kneeCandidate = x[index];
							normKneeCandidate = xNormalized[currentThresholdIndex];
						}
					}
				} else if (curve == Enums.CURVE_TYPE.CONCAVE) {
					if (direction == Enums.DIRECTION.DECREASING) {
						int index = x.length - currentThresholdIndex - 1;
						if (index >= 0 && index < x.length) {
							kneeCandidate = x[index];
							normKneeCandidate = xNormalized[currentThresholdIndex];
						}
					} else {
						kneeCandidate = x[currentThresholdIndex];
						normKneeCandidate = xNormalized[currentThresholdIndex];
					}
				}

				if (kneeCandidate != null && normKneeCandidate != null) {
					// Add the y value at the knee
					Double yAtKnee = findYForX(x, y, kneeCandidate);
					Double yNormAtKnee = findYForX(xNormalized, yNormalized, normKneeCandidate);

					if (yAtKnee != null && yNormAtKnee != null && !allKnees.contains(kneeCandidate)) {
						allKneesY.add(yAtKnee);
						allNormKneesY.add(yNormAtKnee);
						allKnees.add(kneeCandidate);
						allNormKnees.add(normKneeCandidate);
					}

					// If detecting in offline mode, return the first knee found
					if (!online) {
						this.knee = kneeCandidate;
						this.normKnee = normKneeCandidate;
						return;
					} else {
						this.knee = kneeCandidate;
						this.normKnee = normKneeCandidate;
					}
				}
			}
		}

		if (allKnees.isEmpty()) {
			this.knee = null;
			this.normKnee = null;
		}
	}

	/**
	 * Check if array contains value
	 */
	private boolean contains(int[] array, int value) {
		for (int v : array) {
			if (v == value)
				return true;
		}
		return false;
	}

	/**
	 * Find Y value for given X value in arrays
	 */
	private Double findYForX(double[] xArray, double[] yArray, double targetX) {
		for (int i = 0; i < xArray.length; i++) {
			if (Math.abs(xArray[i] - targetX) < 1e-10) {
				return yArray[i];
			}
		}
		return null;
	}

	// Getter methods
	public Double getKnee() {
		return knee;
	}

	public Double getNormKnee() {
		return normKnee;
	}

	public Double getKneeY() {
		return kneeY;
	}

	public Double getNormKneeY() {
		return normKneeY;
	}

	public Set<Double> getAllKnees() {
		return new HashSet<>(allKnees);
	}

	public Set<Double> getAllNormKnees() {
		return new HashSet<>(allNormKnees);
	}

	public List<Double> getAllKneesY() {
		return new ArrayList<>(allKneesY);
	}

	public List<Double> getAllNormKneesY() {
		return new ArrayList<>(allNormKneesY);
	}

	// Getters for enum fields
	public Enums.CURVE_TYPE getCurve() {
		return curve;
	}

	public Enums.DIRECTION getDirection() {
		return direction;
	}

	// Elbow aliases (elbow and knee are interchangeable)
	public Double getElbow() {
		return getKnee();
	}

	public Double getNormElbow() {
		return getNormKnee();
	}

	public Double getElbowY() {
		return getKneeY();
	}

	public Double getNormElbowY() {
		return getNormKneeY();
	}

	public Set<Double> getAllElbows() {
		return getAllKnees();
	}

	public Set<Double> getAllNormElbows() {
		return getAllNormKnees();
	}

	public List<Double> getAllElbowsY() {
		return getAllKneesY();
	}

	public List<Double> getAllNormElbowsY() {
		return getAllNormKneesY();
	}

	// Additional getters for arrays (for testing/debugging)
	public double[] getX() {
		return Arrays.copyOf(x, x.length);
	}

	public double[] getY() {
		return Arrays.copyOf(y, y.length);
	}

	public double[] getXNormalized() {
		return Arrays.copyOf(xNormalized, xNormalized.length);
	}

	public double[] getYNormalized() {
		return Arrays.copyOf(yNormalized, yNormalized.length);
	}

	public double[] getXDifference() {
		return Arrays.copyOf(xDifference, xDifference.length);
	}

	public double[] getYDifference() {
		return Arrays.copyOf(yDifference, yDifference.length);
	}

}