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
 * Implementation of the &quot;minimize drop&quot; MaxSendRate calculation strategy.
 * 
 * @author Ruediger Gad
 *
 */
public class MinimizeDropCalculator extends MaxSendRateCalculator {

	@Override
	public double calculateMaxSendRate(double receivedRate, double sentRate,
			double droppedRate) {
		if (droppedRate > (sentRate * (threshold / 100.0))) {
			decrementMaxSendRate(sentRate);
		}
		
		return maxSendRate;
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public double getDefaultThreshold() {
		return 4;
	}

}
