/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.manager.single;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import clojure.lang.Ratio;
import drepcap.frontend.conf.DefaultValues;
import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.self_adaptivity.analyzer.single.MaxSendRateCalculationApproach;
import drepcap.frontend.self_adaptivity.analyzer.single.MaxSendRateCalculator;
import drepcap.frontend.self_adaptivity.analyzer.single.MinimizeDropCalculator;
import drepcap.frontend.self_adaptivity.analyzer.single.MinimizeReceivedSendDifferenceCalculator;
import drepcap.frontend.self_adaptivity.filter_generator.SingleSensorCaptureRatioToPcapFilterGenerator;
import drepcap.frontend.self_adaptivity.planner.SingleSensorCaptureRatioCalculator;
import drepcap.frontend.util.CaptureRatioHelper;

/**
 * 
 * Implementation of a self-adaptivity manager for a single sensor scenario.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorSamplingSelfAdaptivityManager extends
		AbstractSingleSelfAdaptivityManager {

	private double maxSendRate = -1;
	private double maxSendRateLowerBound = -1;
	private double theoreticCaptureRatio = -1;
	private Ratio actualCaptureRatio;
	private int postCommandInactivity = DefaultValues.DEFAULT_POST_COMMAND_INACTIVITY;
	private int invovationCounter = 0;
	private int granularity = (int) Math.pow(2,
			DefaultValues.DEFAULT_GRANULARITY_EXPONENT);
	private Ratio lowerFilterOffset;

	private MaxSendRateCalculator maxSendRateCalculator;
	private SingleSensorCaptureRatioCalculator captureRatioCalculator = new SingleSensorCaptureRatioCalculator();
	private SingleSensorCaptureRatioToPcapFilterGenerator filterGenerator = new SingleSensorCaptureRatioToPcapFilterGenerator();

	private String lastFilter = "";

	private String outputPrefix = "";

	private List<SingleManagerStatsReceiver> statsReceiver = new ArrayList<>();

	public SingleSensorSamplingSelfAdaptivityManager(JmsAdapter jmsSensorAdapter) {
		this(jmsSensorAdapter, MaxSendRateCalculationApproach.MinimizeDrop);
	}

	public SingleSensorSamplingSelfAdaptivityManager(
			JmsAdapter jmsSensorAdapter,
			MaxSendRateCalculationApproach maxSendCalculationApproach) {
		super(jmsSensorAdapter);
		setMaxSendRateCalculator(maxSendCalculationApproach);
		lowerFilterOffset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
	}

	@Override
	protected void processPerformanceData(double droppedRate,
			double failedRate, double receivedRate, double sentRate) {
		if (invovationCounter > 0) {
			invovationCounter--;
			return;
		}

		// First, we determine the maximum send rate.
		// We use the maximum send rate as upper bound for the possible sensor
		// performance.
		maxSendRate = maxSendRateCalculator.calculateMaxSendRate(receivedRate,
				sentRate, droppedRate);
		// We also calculate a lower bound.
		maxSendRateLowerBound = maxSendRateCalculator.getLowerBound();
		// As long as the actual performance is within these bounds no actions
		// should be triggered. This is done to stabilize the filter rules.
		if (receivedRate <= maxSendRate
				&& receivedRate >= maxSendRateLowerBound) {
			return;
		}
		System.out.println(outputPrefix + "MaxSendRate: " + maxSendRate);

		theoreticCaptureRatio = captureRatioCalculator
				.calculateTheoreticCaptureRatio(receivedRate, sentRate,
						maxSendRate);
		System.out.println(outputPrefix + "Theoretic capture ratio: "
				+ theoreticCaptureRatio);

		actualCaptureRatio = CaptureRatioHelper.calculateActualCaptureRatio(
				theoreticCaptureRatio, granularity);
		System.out.println(outputPrefix + "Actual capture ratio: "
				+ actualCaptureRatio.toString() + " = "
				+ actualCaptureRatio.decimalValue());

		for (SingleManagerStatsReceiver rcvr : statsReceiver) {
			rcvr.statsUpdated(maxSendRate, theoreticCaptureRatio,
					actualCaptureRatio, lowerFilterOffset);
		}

		String filter = filterGenerator.generatePcapFilterFromCaptureRatio(
				actualCaptureRatio, lowerFilterOffset);
		if (filter.compareTo(lastFilter) != 0) {
			try {
				if (filter != null && filter.compareTo("") != 0) {
					System.out.println(outputPrefix + "Setting filter: "
							+ filter);
					if (lastFilter.compareTo("") == 0) {
						jmsSensorAdapter.sendCommand("add-filter " + filter);
					} else {
						jmsSensorAdapter.sendCommand("replace-filter "
								+ lastFilter + " with-filter " + filter);
					}
				} else {
					jmsSensorAdapter.sendCommand("remove-last-filter");
				}
				lastFilter = filter;
				invovationCounter = postCommandInactivity;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

	}

	public double getMaxSendRate() {
		return maxSendRate;
	}

	public double getTheoreticSampledSentRate() {
		if (theoreticCaptureRatio > 0.0 && theoreticCaptureRatio < 1.0) {
			return getTheoreticUnsampledSentRate() * theoreticCaptureRatio;
		}
		return -1;
	}

	public double getTheoreticUnsampledSentRate() {
		return captureRatioCalculator.getTheoreticUnsampledSentRate();
	}

	@Override
	public void enable() {
		super.enable();
	}
	
	@Override
	public void disable() {
	    super.disable();

        try {
            jmsSensorAdapter.sendCommand("hot-standby");
            lastFilter = "less 1";
        } catch (JMSException e) {
            e.printStackTrace();
        }
	    
        maxSendRate = -1;
        theoreticCaptureRatio = -1;
        invovationCounter = 0;
        maxSendRateCalculator.reset();
        captureRatioCalculator.reset();
	}

	public void setMaxSendRateCalculator(MaxSendRateCalculationApproach ma) {
		switch (ma) {
		case MinimizeDrop:
			maxSendRateCalculator = new MinimizeDropCalculator();
			break;
		case MinimizeReceiveSendDifference:
			maxSendRateCalculator = new MinimizeReceivedSendDifferenceCalculator();
			break;
		default:
			System.out.println(outputPrefix
					+ "Defaulting to MinimizeDropCalculator.");
			maxSendRateCalculator = new MinimizeDropCalculator();
			break;
		}
	}

	public double getMaxSendRateThreshold() {
		return maxSendRateCalculator.getThreshold();
	}

	public void setMaxSendThreshold(double val) {
		maxSendRateCalculator.setThreshold(val);
	}

	public double getLowerBoundThreshold() {
		return maxSendRateCalculator.getLowerBoundThreshold();
	}

	public void setLowerBoundThreshold(double val) {
		maxSendRateCalculator.setLowerBoundThreshold(val);
	}

	public double getMaxSendRateLowerBound() {
		return maxSendRateLowerBound;
	}

	public void setMaxSendRateDelta(int val) {
		maxSendRateCalculator.setDelta(val);
	}

	public void setPostCommandInactivity(int val) {
		postCommandInactivity = val;
	}

	public void setLowerBound(int val) {
		setLowerBoundThreshold(val / 100.0);
	}

	public void setGranularityFromExponent(int exponent) {
		granularity = (int) Math.pow(2, exponent);
	}

	public Ratio getActualCaptureRatio() {
		return actualCaptureRatio;
	}

	public Ratio getLowerFilterOffset() {
		return lowerFilterOffset;
	}

	public void setLowerFilterOffset(Ratio lowerFilterOffset) {
		this.lowerFilterOffset = lowerFilterOffset;
	}

	public void setOutputPrefix(String prefix) {
		outputPrefix = prefix;
	}

	public void addStatsReceiver(SingleManagerStatsReceiver rcvr) {
		statsReceiver.add(rcvr);
	}
	
	public void setLastFilter(String expr) {
		lastFilter = expr;
	}
}
