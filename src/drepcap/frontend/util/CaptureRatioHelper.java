/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.util;

import java.math.BigInteger;

import clojure.lang.Ratio;

/**
 * 
 * Helper class for calculating capture ratios.
 * 
 * @author Ruediger Gad
 *
 */
public class CaptureRatioHelper {

	public static Ratio calculateActualCaptureRatio(double captureRatio,
			int denominator) {
		if (captureRatio <= 0) {
			return new Ratio(BigInteger.ZERO, BigInteger.ONE);
		}
		if (captureRatio >= 1) {
			return new Ratio(BigInteger.ONE, BigInteger.ONE);
		}
		if (denominator <= 0) {
			System.err.println("Invalid denominator value: " + denominator);
			return new Ratio(BigInteger.valueOf(-1), BigInteger.ONE);
		}

		double singleStep = 1.0 / denominator;
		int numerator = denominator - 1;

		for (int i = 1; i < denominator; i++) {
			if (Math.abs((singleStep * i) - captureRatio) < (singleStep / 2)) {
				numerator = i - 1;
				break;
			}
		}

		return new Ratio(BigInteger.valueOf(numerator),
				BigInteger.valueOf(denominator));
	}
}
