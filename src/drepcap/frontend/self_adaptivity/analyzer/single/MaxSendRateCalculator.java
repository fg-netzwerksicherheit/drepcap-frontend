/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.analyzer.single;

import drepcap.frontend.conf.DefaultValues;

/**
 * 
 * Abstract base class for calculating the MaxSendRate.
 * 
 * @author Ruediger Gad
 *
 */
public abstract class MaxSendRateCalculator {

	protected double maxSendRate = -1;
	protected double threshold;
	protected double lowerBoundThreshold;
	protected int maxSendRateDelta;

	public MaxSendRateCalculator() {
		threshold = getDefaultThreshold();
		lowerBoundThreshold = getDefaultLowerBoundThreshold();
		maxSendRateDelta = getDefaultMaxSendRateDelta();
	}

	public abstract double calculateMaxSendRate(double receivedRate,
			double sentRate, double droppedRate);

	public abstract double getDefaultThreshold();

	public double getDefaultLowerBoundThreshold() {
		return DefaultValues.DEFAULT_LOWER_BOUND / 100.0;
	}
	
	public int getDefaultMaxSendRateDelta() {
		return DefaultValues.DEFAULT_MAX_SEND_RATE_DELTA;
	}

	public double getThreshold() {
		return threshold;
	}

	public void reset() {
		maxSendRate = -1;
	}

	public double getLowerBound() {
		return maxSendRate - (maxSendRate * lowerBoundThreshold);
	}

	public void setThreshold(double val) {
		threshold = val;
	}

	public double getLowerBoundThreshold() {
		return lowerBoundThreshold;
	}

	public void setLowerBoundThreshold(double val) {
		lowerBoundThreshold = val;
	}

	public void setDelta(int val) {
		maxSendRateDelta = val;
	}
	
	protected void decrementMaxSendRate(double initialValue) {
		if (maxSendRate == -1) {
			maxSendRate = initialValue;
		} else {
			maxSendRate = maxSendRate * (1 - (maxSendRateDelta / 100.0));
		}
	}
}
