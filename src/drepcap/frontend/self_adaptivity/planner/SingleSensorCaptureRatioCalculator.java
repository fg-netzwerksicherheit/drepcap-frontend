/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.planner;

/**
 * 
 * Implementation for calculating the capture ratio.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorCaptureRatioCalculator {

	private double theoreticCaptureRatio = 1;
	private double theoreticUnsampledSentRate;
	private double theoreticUnsampledReceivedRate;

	public double calculateTheoreticCaptureRatio(double receivedRate, double sentRate, double maxSendRate) {
		theoreticUnsampledSentRate = sentRate / theoreticCaptureRatio;
		theoreticUnsampledReceivedRate = receivedRate / theoreticCaptureRatio;
		
		if (maxSendRate > 0 && receivedRate > maxSendRate) {
			theoreticCaptureRatio = maxSendRate / theoreticUnsampledReceivedRate;
		} else if (maxSendRate > 0 && theoreticUnsampledSentRate > maxSendRate) {
			theoreticCaptureRatio = maxSendRate / theoreticUnsampledSentRate;
		} else {
			theoreticCaptureRatio = 1;	
		}
		
		return theoreticCaptureRatio;
	}

	public void reset() {
		theoreticCaptureRatio = 1;
	}

	public double getTheoreticUnsampledSentRate() {
		return theoreticUnsampledSentRate;
	}

	public double getTheoreticUnsampledReceivedRate() {
		return theoreticUnsampledReceivedRate;
	}
}
