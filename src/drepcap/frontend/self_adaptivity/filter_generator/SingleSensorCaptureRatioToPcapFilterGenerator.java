/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.filter_generator;

import clojure.lang.Ratio;

/**
 * 
 * Calculates the capture ratio for a single sensor.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorCaptureRatioToPcapFilterGenerator {

	public String generatePcapFilterFromCaptureRatio(Ratio actualCaptureRatio,
			Ratio lowerFilterOffset) {
		if (actualCaptureRatio.numerator.intValue() == 0) {
			return "less 1";
		}

		if (actualCaptureRatio.doubleValue() >= 1.0
				&& lowerFilterOffset.doubleValue() > 0.0
				&& lowerFilterOffset.doubleValue() < 1.0) {
			return "ip [4:2] & "
					+ (lowerFilterOffset.denominator.intValue() - 1) + " >= "
					+ lowerFilterOffset.numerator.intValue();
		}

		if (actualCaptureRatio.denominator.intValue() == 1) {
			if (actualCaptureRatio.numerator.intValue() == 1) {
				return "";
			} else if (actualCaptureRatio.numerator.intValue() <= -1) {
				System.err.println("Got invalid capture ratio: "
						+ actualCaptureRatio.toString());
				return "";
			}
		}

		if (lowerFilterOffset.numerator.intValue() == 0) {
			return "ip [4:2] & "
					+ (actualCaptureRatio.denominator.intValue() - 1) + " < "
					+ actualCaptureRatio.numerator.intValue();
		}

		if ((lowerFilterOffset.numerator.intValue() + actualCaptureRatio.numerator
				.intValue()) > actualCaptureRatio.denominator.intValue()) {
			return "ip [4:2] & "
					+ (actualCaptureRatio.denominator.intValue() - 1) + " >= "
					+ lowerFilterOffset.numerator.intValue();
		} else {
			return "(ip [4:2] & "
					+ (actualCaptureRatio.denominator.intValue() - 1)
					+ " >= "
					+ lowerFilterOffset.numerator.intValue()
					+ ") and (ip [4:2] & "
					+ (actualCaptureRatio.denominator.intValue() - 1)
					+ " < "
					+ (lowerFilterOffset.numerator.intValue() + actualCaptureRatio.numerator
							.intValue()) + ")";
		}
	}
}
