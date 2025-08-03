package com.github.shravanasati.kneedle4j;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class KneeLocatorTest {

	/**
	 * Helper method to check if two doubles are close within a relative tolerance
	 */
	private boolean isClose(double a, double b, double relTol) {
		if (a == b)
			return true;
		if (Double.isInfinite(a) || Double.isInfinite(b))
			return false;
		if (Double.isNaN(a) || Double.isNaN(b))
			return false;

		double diff = Math.abs(a - b);
		double largest = Math.max(Math.abs(a), Math.abs(b));
		return diff <= relTol * largest;
	}

	/**
	 * Helper method to truncate array (equivalent to Python's [:-3])
	 */
	private double[] truncateArray(double[] arr, int elementsToRemove) {
		int newLength = arr.length - elementsToRemove;
		return Arrays.copyOf(arr, newLength);
	}

	/**
	 * Helper method to divide array by a scalar
	 */
	private double[] divideArray(double[] arr, double divisor) {
		double[] result = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			result[i] = arr[i] / divisor;
		}
		return result;
	}

	@Test
	public void testFigure2Interp1d() {
		// From the kneedle manuscript
		DataGenerator.DataPair data = DataGenerator.figure2();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be close to 0.22", isClose(kl.getKnee(), 0.22, 0.05));
		assertTrue("Elbow should be close to 0.22", isClose(kl.getElbow(), 0.22, 0.05));
		assertTrue("Norm elbow should be close to knee", isClose(kl.getNormElbow(), kl.getKnee(), 0.05));
	}

	@Test
	public void testFigure2Polynomial() {
		// From the kneedle manuscript
		DataGenerator.DataPair data = DataGenerator.figure2();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be close to 0.22", isClose(kl.getKnee(), 0.22, 0.05));
		assertTrue("Elbow should be close to 0.22", isClose(kl.getElbow(), 0.22, 0.05));
		assertTrue("Norm elbow should be close to knee", isClose(kl.getNormElbow(), kl.getKnee(), 0.05));
	}

	@Test
	public void testNoisyGaussian() {
		// From the Kneedle manuscript
		DataGenerator.DataPair data = DataGenerator.noisyGaussian(50.0, 10.0, 1000, 42L);
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, true, 11);

		assertTrue("Knee should be close to 63.0", isClose(kl.getKnee(), 63.0, 0.01));
	}

	@Test
	public void testConcaveIncreasingInterp1d() {
		// test a concave increasing function
		DataGenerator.DataPair data = DataGenerator.concaveIncreasing();
		KneeLocator kn = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 2", kn.getKnee().equals(2.0));
	}

	@Test
	public void testConcaveIncreasingPolynomial() {
		// test a concave increasing function
		DataGenerator.DataPair data = DataGenerator.concaveIncreasing();
		KneeLocator kn = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 2", kn.getKnee().equals(2.0));
	}

	@Test
	public void testConcaveDecreasingInterp1d() {
		// test a concave decreasing function
		DataGenerator.DataPair data = DataGenerator.concaveDecreasing();
		KneeLocator kn = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 7", kn.getKnee().equals(7.0));
	}

	@Test
	public void testConcaveDecreasingPolynomial() {
		// test a concave decreasing function
		DataGenerator.DataPair data = DataGenerator.concaveDecreasing();
		KneeLocator kn = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 7", kn.getKnee().equals(7.0));
	}

	@Test
	public void testConvexIncreasingInterp1d() {
		// test a convex increasing function
		DataGenerator.DataPair data = DataGenerator.convexIncreasing();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 7", kl.getKnee().equals(7.0));
	}

	@Test
	public void testConvexIncreasingPolynomial() {
		// test a convex increasing function
		DataGenerator.DataPair data = DataGenerator.convexIncreasing();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 7", kl.getKnee().equals(7.0));
	}

	@Test
	public void testConvexDecreasingInterp1d() {
		// test a convex decreasing function
		DataGenerator.DataPair data = DataGenerator.convexDecreasing();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 2", kl.getKnee().equals(2.0));
	}

	@Test
	public void testConvexDecreasingPolynomial() {
		// test a convex decreasing function
		DataGenerator.DataPair data = DataGenerator.convexDecreasing();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 2", kl.getKnee().equals(2.0));
	}

	@Test
	public void testConcaveIncreasingTruncatedInterp1d() {
		// test a truncated concave increasing function
		DataGenerator.DataPair data = DataGenerator.concaveIncreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 0.2", kl.getKnee().equals(0.2));
	}

	@Test
	public void testConcaveIncreasingTruncatedPolynomial() {
		// test a truncated concave increasing function
		DataGenerator.DataPair data = DataGenerator.concaveIncreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 0.2", kl.getKnee().equals(0.2));
	}

	@Test
	public void testConcaveDecreasingTruncatedInterp1d() {
		// test a truncated concave decreasing function
		DataGenerator.DataPair data = DataGenerator.concaveDecreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 0.4", kl.getKnee().equals(0.4));
	}

	@Test
	public void testConcaveDecreasingTruncatedPolynomial() {
		// test a truncated concave decreasing function
		DataGenerator.DataPair data = DataGenerator.concaveDecreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 0.4", kl.getKnee().equals(0.4));
	}

	@Test
	public void testConvexIncreasingTruncatedInterp1d() {
		// test a truncated convex increasing function
		DataGenerator.DataPair data = DataGenerator.convexIncreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 0.4", kl.getKnee().equals(0.4));
	}

	@Test
	public void testConvexIncreasingTruncatedPolynomial() {
		// test a truncated convex increasing function
		DataGenerator.DataPair data = DataGenerator.convexIncreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 0.4", kl.getKnee().equals(0.4));
	}

	@Test
	public void testConvexDecreasingTruncatedInterp1d() {
		// test a truncated convex decreasing function
		DataGenerator.DataPair data = DataGenerator.convexDecreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 0.2", kl.getKnee().equals(0.2));
	}

	@Test
	public void testConvexDecreasingTruncatedPolynomial() {
		// test a truncated convex decreasing function
		DataGenerator.DataPair data = DataGenerator.convexDecreasing();
		double[] xTrunc = divideArray(truncateArray(data.getX(), 3), 10.0);
		double[] yTrunc = divideArray(truncateArray(data.getY(), 3), 10.0);

		KneeLocator kl = new KneeLocator(xTrunc, yTrunc, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 0.2", kl.getKnee().equals(0.2));
	}

	@Test
	public void testConvexDecreasingBumpyInterp1d() {
		// test a bumpy convex decreasing function
		DataGenerator.DataPair data = DataGenerator.bumpy();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be 26", kl.getKnee().equals(26.0));
	}

	@Test
	public void testConvexDecreasingBumpyPolynomial() {
		// test a bumpy convex decreasing function
		DataGenerator.DataPair data = DataGenerator.bumpy();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.POLYNOMIAL, false, 7);

		assertTrue("Knee should be 28", kl.getKnee().equals(28.0));
	}

	@Test
	public void testFlatMaximaS0() {
		// The global maxima has a sequentially equal value in the difference curve
		double[] x = { 0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0 };
		double[] y = { 1, 0.787701317715959, 0.7437774524158126, 0.6559297218155198, 0.5065885797950219,
				0.36749633967789164, 0.2547584187408492, 0.16251830161054173, 0.10395314787701318,
				0.06734992679355783, 0.043923865300146414, 0.027818448023426062, 0.01903367496339678,
				0.013177159590043924, 0.010248901903367497, 0.007320644216691069, 0.005856515373352855,
				0.004392386530014641 };

		// When S=0.0 the first local maximum is found.
		KneeLocator kl = new KneeLocator(x, y, 0.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be close to 1.0", isClose(kl.getKnee(), 1.0, 0.05));
	}

	@Test
	public void testFlatMaximaS1() {
		// The global maxima has a sequentially equal value in the difference curve
		double[] x = { 0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0 };
		double[] y = { 1, 0.787701317715959, 0.7437774524158126, 0.6559297218155198, 0.5065885797950219,
				0.36749633967789164, 0.2547584187408492, 0.16251830161054173, 0.10395314787701318,
				0.06734992679355783, 0.043923865300146414, 0.027818448023426062, 0.01903367496339678,
				0.013177159590043924, 0.010248901903367497, 0.007320644216691069, 0.005856515373352855,
				0.004392386530014641 };

		// When S=1.0 the global maximum is found.
		KneeLocator kl = new KneeLocator(x, y, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee should be close to 8.0", isClose(kl.getKnee(), 8.0, 0.05));
	}

	@Test
	public void testYValues() {
		// Test the y value
		DataGenerator.DataPair data = DataGenerator.figure2();
		KneeLocator kl = new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertTrue("Knee Y should be close to 1.897", isClose(kl.getKneeY(), 1.897, 0.03));
		assertTrue("All knees Y[0] should be close to 1.897", isClose(kl.getAllKneesY().get(0), 1.897, 0.03));
		assertTrue("Norm knee Y should be close to 0.758", isClose(kl.getNormKneeY(), 0.758, 0.03));
		assertTrue("All norm knees Y[0] should be close to 0.758", isClose(kl.getAllNormKneesY().get(0), 0.758, 0.03));

		assertTrue("Elbow Y should be close to 1.897", isClose(kl.getElbowY(), 1.897, 0.03));
		assertTrue("All elbows Y[0] should be close to 1.897", isClose(kl.getAllElbowsY().get(0), 1.897, 0.03));
		assertTrue("Norm elbow Y should be close to 0.758", isClose(kl.getNormElbowY(), 0.758, 0.03));
		assertTrue("All norm elbows Y[0] should be close to 0.758",
				isClose(kl.getAllNormElbowsY().get(0), 0.758, 0.03));
	}

	@Test
	public void testYNoKnee() {
		// Test the y value, if there is no knee found.
		double[] x = { 1, 2, 3 };
		double[] y = { 0.90483742, 0.81873075, 0.74081822 };

		KneeLocator kl = new KneeLocator(x, y, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.DECREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);

		assertNull("Knee Y should be null", kl.getKneeY());
		assertNull("Norm knee Y should be null", kl.getNormKneeY());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInterpMethod() {
		// Test that the interp_method argument is valid.
		DataGenerator.DataPair data = DataGenerator.figure2();
		new KneeLocator(data.getX(), data.getY(), 1.0,
				Enums.CURVE_TYPE.CONCAVE, Enums.DIRECTION.INCREASING, null, false, 7);
	}

	@Test
	public void testXEqualsY() {
		// Test that knee is null when no maxima are found
		double[] x = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		double[] y = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		KneeLocator kl = new KneeLocator(x, y);
		assertNull("Knee should be null", kl.getKnee());
	}

	@Test
	public void testLogistic() {
		double[] y = {
				2.00855493e-45, 1.10299045e-43, 4.48168384e-42, 1.22376580e-41, 5.10688883e-40, 1.18778110e-38,
				5.88777891e-35, 4.25317895e-34, 4.06507035e-33, 6.88084518e-32, 2.99321831e-31, 1.13291723e-30,
				1.05244482e-28, 2.67578448e-27, 1.22522190e-26, 2.36517846e-26, 8.30369408e-26, 1.24303033e-25,
				2.27726918e-25, 1.06330422e-24, 5.55017673e-24, 1.92068553e-23, 3.31361011e-23, 1.13575247e-22,
				1.75386416e-22, 6.52680518e-22, 2.05106011e-21, 6.37285545e-21, 4.16125535e-20, 1.12709507e-19,
				5.75853420e-19, 1.73333796e-18, 2.70099890e-18, 7.53254646e-18, 1.38139433e-17, 3.60081965e-17,
				8.08419977e-17, 1.86378584e-16, 5.36224556e-16, 8.89404640e-16, 2.34045104e-15, 4.72168880e-15,
				6.84378992e-15, 2.26898430e-14, 3.10087652e-14, 2.78081199e-13, 1.06479577e-12, 2.81002203e-12,
				4.22067092e-12, 9.27095863e-12, 1.54519738e-11, 4.53347819e-11, 1.35564441e-10, 2.35242087e-10,
				4.45253545e-10, 9.78613696e-10, 1.53140922e-09, 2.81648560e-09, 6.70890436e-09, 1.49724785e-08,
				5.59553565e-08, 1.39510811e-07, 7.64761811e-07, 1.40723957e-06, 4.97638863e-06, 2.12817943e-05,
				3.26471410e-05, 1.02599591e-04, 3.18774179e-04, 5.67297630e-04, 9.22732716e-04, 1.17445643e-03,
				3.59279384e-03, 3.61936491e-02, 6.39493416e-02, 1.29304829e-01, 1.72272215e-01, 3.46945901e-01,
				5.02826602e-01, 6.24800042e-01, 7.38412957e-01, 7.59931663e-01, 7.73374421e-01, 7.91421897e-01,
				8.29325597e-01, 8.57718637e-01, 8.73286061e-01, 8.77056835e-01, 8.93173768e-01, 9.05435646e-01,
				9.17217910e-01, 9.19119179e-01, 9.24810910e-01, 9.26306908e-01, 9.28621233e-01, 9.33855835e-01,
				9.37263027e-01, 9.41651642e-01
		};

		double[] x = new double[y.length];
		for (int i = 0; i < x.length; i++) {
			x[i] = i + 1.0;
		}

		KneeLocator kl = new KneeLocator(x, y, 1.0,
				Enums.CURVE_TYPE.CONVEX, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, true, 7);

		assertTrue("Knee should be 73", kl.getKnee().equals(73.0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidCurveNull() {
		// Test that arguments to curve are valid
		double[] x = { 0, 1, 2 };
		double[] y = { 1, 3, 5 };
		new KneeLocator(x, y, 1.0, null, Enums.DIRECTION.INCREASING, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidDirectionNull() {
		// Test that arguments to direction are valid
		double[] x = { 0, 1, 2 };
		double[] y = { 1, 3, 5 };
		new KneeLocator(x, y, 1.0, Enums.CURVE_TYPE.CONCAVE, null, Enums.INTERPOLATION_METHOD.INTERP1D, false, 7);
	}

	@Test
	public void testFindShapeConcaveIncreasing() {
		// Test that find_shape can detect the right shape of curve line
		DataGenerator.DataPair data = DataGenerator.concaveIncreasing();
		ShapeDetector.ShapeResult result = ShapeDetector.findShape(data.getX(), data.getY());

		assertEquals(Enums.DIRECTION.INCREASING, result.getDirection());
		assertEquals(Enums.CURVE_TYPE.CONCAVE, result.getCurveType());
	}

	@Test
	public void testFindShapeConcaveDecreasing() {
		DataGenerator.DataPair data = DataGenerator.concaveDecreasing();
		ShapeDetector.ShapeResult result = ShapeDetector.findShape(data.getX(), data.getY());

		assertEquals(Enums.DIRECTION.DECREASING, result.getDirection());
		assertEquals(Enums.CURVE_TYPE.CONCAVE, result.getCurveType());
	}

	@Test
	public void testFindShapeConvexDecreasing() {
		DataGenerator.DataPair data = DataGenerator.convexDecreasing();
		ShapeDetector.ShapeResult result = ShapeDetector.findShape(data.getX(), data.getY());

		assertEquals(Enums.DIRECTION.DECREASING, result.getDirection());
		assertEquals(Enums.CURVE_TYPE.CONVEX, result.getCurveType());
	}

	@Test
	public void testFindShapeConvexIncreasing() {
		DataGenerator.DataPair data = DataGenerator.convexIncreasing();
		ShapeDetector.ShapeResult result = ShapeDetector.findShape(data.getX(), data.getY());

		assertEquals(Enums.DIRECTION.INCREASING, result.getDirection());
		assertEquals(Enums.CURVE_TYPE.CONVEX, result.getCurveType());
	}
}
