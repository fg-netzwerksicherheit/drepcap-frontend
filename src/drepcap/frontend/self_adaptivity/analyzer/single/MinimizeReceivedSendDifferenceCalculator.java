/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.analyzer.single;

/**
 * 
 * Implementation of the &quot;minimize receive/send difference&quot; MaxSendRate calculation strategy.
 * 
 * @author Ruediger Gad
 *
 */
public class MinimizeReceivedSendDifferenceCalculator extends MaxSendRateCalculator {
	
	@Override
	public double calculateMaxSendRate(double receivedRate, double sentRate,
			double droppedRate) {
		double difference = receivedRate - sentRate;
		
		if (difference > (sentRate * (threshold / 100.0))) {
			decrementMaxSendRate(sentRate);
		}
		
		return maxSendRate;
	}

	@Override
	public double getDefaultThreshold() {
		return 5;
	}

}
