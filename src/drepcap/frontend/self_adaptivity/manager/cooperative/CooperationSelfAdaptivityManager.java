/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.manager.cooperative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.JMSException;

import clojure.lang.Ratio;
import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.self_adaptivity.analyzer.single.MaxSendRateCalculationApproach;
import drepcap.frontend.self_adaptivity.manager.single.SingleManagerStatsReceiver;
import drepcap.frontend.self_adaptivity.manager.single.SingleSensorSamplingSelfAdaptivityManager;

/**
 * 
 * Manager implementation for handling cooperative self-adaptivity.
 * 
 * @author Ruediger Gad
 *
 */
public class CooperationSelfAdaptivityManager {

	private SortedSet<String> sensorIds = new TreeSet<>();
	private Map<String, JmsAdapter> sensorJmsAdapters = new HashMap<>();
	private Map<String, SingleSensorSamplingSelfAdaptivityManager> singleSelfAdaptivityManagers = new HashMap<>();

	private MaxSendRateCalculationApproach maxSendRateCalculationApproach = MaxSendRateCalculationApproach.MinimizeDrop;

	private List<CooperationStatsUpdateListener> statsUpdateListeners = new ArrayList<>();

	public void addSensor(String id, JmsAdapter jmsAdapter) {
		if (!sensorIds.contains(id)) {
			sensorIds.add(id);
			sensorJmsAdapters.put(id, jmsAdapter);

			SingleSensorSamplingSelfAdaptivityManager selfAdaptivityManager = new SingleSensorSamplingSelfAdaptivityManager(
					jmsAdapter);
			selfAdaptivityManager
					.setMaxSendRateCalculator(maxSendRateCalculationApproach);
			selfAdaptivityManager.setOutputPrefix(id + "> ");
			selfAdaptivityManager
					.addStatsReceiver(new StatusUpdateForwarder(id));
			singleSelfAdaptivityManagers.put(id, selfAdaptivityManager);
		} else {
			throw new RuntimeException("Error adding adapter. "
					+ "Adapter Id \"" + id + "\" already in use.");
		}
	}

	public void addStatusUpdateListener(CooperationStatsUpdateListener listener) {
		statsUpdateListeners.add(listener);
	}

	public void clearStatusUpdateListeners() {
		statsUpdateListeners.clear();
	}

	public void removeSensor(String sensorId) {
		sensorJmsAdapters.remove(sensorId);
	}

	public void enable() {
		System.out.println("Enabling cooperative self-adaptivity...");

		for (String id : sensorIds) {
			System.out.println("Processing sensor: " + id);

			JmsAdapter jmsAdapter = sensorJmsAdapters.get(id);
			SingleSensorSamplingSelfAdaptivityManager selfAdaptivityManager = singleSelfAdaptivityManagers
					.get(id);

			if (id.compareTo(sensorIds.first()) == 0) {
				System.out.println("First sensor...");
				try {
					jmsAdapter.sendCommand("remove-all-filters");
				} catch (JMSException e) {
					e.printStackTrace();
				}
				selfAdaptivityManager.enable();
			} else {
				System.out.println("Subsequent sensor...");
				selfAdaptivityManager.disable();
			}
		}
	}

	public void disable() {
		for (String id : sensorIds) {
			System.out.println("Processing sensor: " + id);

			SingleSensorSamplingSelfAdaptivityManager selfAdaptivityManager = singleSelfAdaptivityManagers
					.get(id);

			selfAdaptivityManager.disable();
		}
	}

	public MaxSendRateCalculationApproach getMaxSendRateCalcualtionApproach() {
		return maxSendRateCalculationApproach;
	}

	public void setMaxSendRateCalculationApproach(
			MaxSendRateCalculationApproach approach) {
		maxSendRateCalculationApproach = approach;

		for (SingleSensorSamplingSelfAdaptivityManager mgr : singleSelfAdaptivityManagers
				.values()) {
			mgr.setMaxSendRateCalculator(maxSendRateCalculationApproach);
		}
	}

	protected String getNextSensorId(String sensorId) {
		SortedSet<String> subSet = sensorIds.tailSet(sensorId);
		Iterator<String> subSetIt = subSet.iterator();
		subSetIt.next();

		if (subSetIt.hasNext()) {
			return subSetIt.next();
		} else {
			return null;
		}
	}

	private class StatusUpdateForwarder implements SingleManagerStatsReceiver {

		private final String sensorId;

		public StatusUpdateForwarder(String sensorId) {
			this.sensorId = sensorId;
		}

		@Override
		public void statsUpdated(double maxSendRate,
				double theoreticCaptureRatio, Ratio actualCaptureRatio,
				Ratio lowerFilterOffset) {
			for (CooperationStatsUpdateListener listener : statsUpdateListeners) {
				listener.statusUpdated(sensorId, maxSendRate,
						theoreticCaptureRatio, actualCaptureRatio,
						lowerFilterOffset);
			}

			String nextSensorId = getNextSensorId(sensorId);

			if (nextSensorId != null) {
				System.out.println(sensorId + "> Found next sensor: "
						+ nextSensorId);

				SingleSensorSamplingSelfAdaptivityManager currentMgr = singleSelfAdaptivityManagers
						.get(sensorId);
				SingleSensorSamplingSelfAdaptivityManager nextMgr = singleSelfAdaptivityManagers
						.get(nextSensorId);

				Ratio currentOffset = currentMgr.getLowerFilterOffset();
				System.out.println(sensorId + "> Current offset: "
						+ currentOffset);

				if (currentOffset.doubleValue() > 0.0
						&& currentOffset.doubleValue() < 1.0) {
					System.out.println(sensorId + "> Adding...");
					nextMgr.setLowerFilterOffset(new Ratio(
							actualCaptureRatio.numerator
									.add(currentOffset.numerator),
							actualCaptureRatio.denominator));
				} else {
					nextMgr.setLowerFilterOffset(actualCaptureRatio);
				}

				if (actualCaptureRatio.doubleValue() < 1.0) {
					System.out.println(sensorId
							+ "> Actual capture ratio lower 1: "
							+ actualCaptureRatio.toString());
					if (!nextMgr.isEnabled()) {
						nextMgr.enable();
					}
				} else {
					if (nextMgr.isEnabled()) {
						nextMgr.disable();
					}
				}
			}
		}
	}
}
