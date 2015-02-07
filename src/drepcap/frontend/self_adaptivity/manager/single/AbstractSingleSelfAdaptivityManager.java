/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.manager.single;

import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.jms.receiver.StatsDataReceiver;

/**
 * 
 * Abstract base class for a single sensor self-adaptivity scenario.
 * 
 * @author Ruediger Gad
 *
 */
public abstract class AbstractSingleSelfAdaptivityManager implements
		SingleSelfAdaptivityManager {

	private boolean enabled = false;
	protected JmsAdapter jmsSensorAdapter;

	public AbstractSingleSelfAdaptivityManager(JmsAdapter jmsSensorAdapter) {
		this.jmsSensorAdapter = jmsSensorAdapter;
		jmsSensorAdapter.addStatsDataReceiver(new StatsDataReceiver() {

			@Override
			public void processSingleSensorStatsData(double droppedRate,
					double failedRate, double receivedRate, double sentRate,
					double droppedRateMean, double failedRateMean,
					double receivedRateMean, double sentRateMean) {
				addPerformanceData(droppedRateMean, failedRateMean,
						receivedRateMean, sentRateMean);
			}
		});
	}

	@Override
	public void addPerformanceData(double droppedRate, double failedRate,
			double receivedRate, double sentRate) {
		if (!enabled) {
			return;
		}

		processPerformanceData(droppedRate, failedRate, receivedRate, sentRate);
	}

	protected abstract void processPerformanceData(double droppedRate,
			double failedRate, double receivedRate, double sentRate);

	public boolean isEnabled() {
		return enabled;
	}

	public void disable() {
	    enabled = false;
	}
	
	public void enable() {
		enabled = true;
	}
}
