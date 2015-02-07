/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.jms.receiver;

/**
 * 
 * Interface for receiving stats data from the a single sensor.
 * 
 * @author Ruediger Gad
 *
 */
public interface StatsDataReceiver {

	public void processSingleSensorStatsData(double droppedRate,
			double failedRate, double receivedRate, double sentRate,
			double droppedRateMean, double failedRateMean,
			double receivedRateMean, double sentRateMean);
}
