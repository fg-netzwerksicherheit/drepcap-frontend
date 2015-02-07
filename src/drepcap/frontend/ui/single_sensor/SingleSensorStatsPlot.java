/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.single_sensor;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import drepcap.frontend.self_adaptivity.manager.single.SingleSensorSamplingSelfAdaptivityManager;

/**
 * 
 * UI component for displaying a stats plot for a single sensor.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorStatsPlot extends Composite {

	private CircularBufferDataProvider receivedDataProvider;
	private CircularBufferDataProvider droppedDataProvider;
	private CircularBufferDataProvider sentDataProvider;
	private CircularBufferDataProvider failedDataProvider;
	private CircularBufferDataProvider sendLimitDataProvider;
	private CircularBufferDataProvider lowerSendLimitDataProvider;
	private CircularBufferDataProvider sentPredictedDataProvider;
	private int sentPredictBufferSize = 3;
	private CircularBufferDataProvider theoreticSampledSentRateDataProvider;
	private CircularBufferDataProvider theoreticUnsampledSentRateDataProvider;
	private CircularBufferDataProvider receivedMeanDataProvider;
	private CircularBufferDataProvider droppedMeanDataProvider;
	private CircularBufferDataProvider sentMeanDataProvider;
	private CircularBufferDataProvider failedMeanDataProvider;

	private SingleSensorSamplingSelfAdaptivityManager singleSamplingSelfAdaptivityManager;

	private DescriptiveStatistics sentDataStatistics = new DescriptiveStatistics(
			5);

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SingleSensorStatsPlot(Composite parent, int style) {
		super(parent, style);

		Canvas chartCanvas = new Canvas(this, SWT.NONE);
		LightweightSystem lwSys = new LightweightSystem(chartCanvas);

		XYGraph statsXyGraph = new XYGraph();
		statsXyGraph.primaryXAxis.setAutoFormat(true);
		statsXyGraph.primaryXAxis.setAutoScale(true);
		statsXyGraph.primaryXAxis.setDateEnabled(true);
		statsXyGraph.primaryXAxis.setTitle("");
		statsXyGraph.primaryYAxis.setAutoScale(true);
		statsXyGraph.primaryYAxis.setLogScale(false);
		statsXyGraph.primaryYAxis.setTitle("[occurrences/s]");
		statsXyGraph.setShowTitle(false);
		lwSys.setContents(statsXyGraph);

		final int bufferSize = 60;

		droppedMeanDataProvider = new CircularBufferDataProvider(true);
		droppedMeanDataProvider.setBufferSize(bufferSize);
		Trace t = addTrace(statsXyGraph, droppedMeanDataProvider, "",
				SWT.COLOR_RED, TraceType.AREA, PointStyle.NONE);
		t.setAreaAlpha(50);

		droppedDataProvider = new CircularBufferDataProvider(true);
		droppedDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, droppedDataProvider, "Drop.", SWT.COLOR_RED,
				TraceType.SOLID_LINE, PointStyle.CROSS);

		failedMeanDataProvider = new CircularBufferDataProvider(true);
		failedMeanDataProvider.setBufferSize(bufferSize);
		t = addTrace(statsXyGraph, failedMeanDataProvider, "",
				SWT.COLOR_MAGENTA, TraceType.AREA, PointStyle.NONE);
		t.setAreaAlpha(75);

		failedDataProvider = new CircularBufferDataProvider(true);
		failedDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, failedDataProvider, "Fail.", SWT.COLOR_MAGENTA,
				TraceType.SOLID_LINE, PointStyle.CROSS);

		receivedMeanDataProvider = new CircularBufferDataProvider(true);
		receivedMeanDataProvider.setBufferSize(bufferSize);
		t = addTrace(statsXyGraph, receivedMeanDataProvider, "",
				SWT.COLOR_BLUE, TraceType.AREA, PointStyle.NONE);
		t.setAreaAlpha(50);

		receivedDataProvider = new CircularBufferDataProvider(true);
		receivedDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, receivedDataProvider, "Rec.", SWT.COLOR_BLUE,
				TraceType.SOLID_LINE, PointStyle.XCROSS);

		sentMeanDataProvider = new CircularBufferDataProvider(true);
		sentMeanDataProvider.setBufferSize(bufferSize);
		t = addTrace(statsXyGraph, sentMeanDataProvider, "",
				SWT.COLOR_DARK_GREEN, TraceType.AREA, PointStyle.NONE);
		t.setAreaAlpha(75);

		sentDataProvider = new CircularBufferDataProvider(true);
		sentDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, sentDataProvider, "Sent", SWT.COLOR_DARK_GREEN,
				TraceType.SOLID_LINE, PointStyle.CROSS);

		sendLimitDataProvider = new CircularBufferDataProvider(true);
		sendLimitDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, sendLimitDataProvider, "S. Lim.", SWT.COLOR_RED,
				TraceType.DASH_LINE, PointStyle.NONE);

		lowerSendLimitDataProvider = new CircularBufferDataProvider(true);
		lowerSendLimitDataProvider.setBufferSize(bufferSize);
		addTrace(statsXyGraph, lowerSendLimitDataProvider, "Low S.L.",
				SWT.COLOR_DARK_GRAY, TraceType.DASH_LINE, PointStyle.NONE);

		sentPredictedDataProvider = new CircularBufferDataProvider(true);
		sentPredictedDataProvider.setBufferSize(sentPredictBufferSize);
		addTrace(statsXyGraph, sentPredictedDataProvider, "S. Pred.",
				SWT.COLOR_GREEN, TraceType.DASH_LINE, PointStyle.NONE);

		theoreticSampledSentRateDataProvider = new CircularBufferDataProvider(
				true);
		theoreticSampledSentRateDataProvider.setBufferSize(bufferSize);
		// addTrace(statsXyGraph, theoreticSampledSentRateDataProvider,
		// "Sampled",
		// SWT.COLOR_GRAY, TraceType.DASH_LINE, PointStyle.NONE);

		theoreticUnsampledSentRateDataProvider = new CircularBufferDataProvider(
				true);
		theoreticUnsampledSentRateDataProvider.setBufferSize(bufferSize);
		// addTrace(statsXyGraph, theoreticUnsampledSentRateDataProvider,
		// "Unsampled", SWT.COLOR_BLACK, TraceType.DASH_LINE,
		// PointStyle.NONE);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void updateData(final double droppedRate, final double failedRate,
			final double receivedRate, final double sentRate,
			final double droppedRateMean, final double failedRateMean,
			final double receivedRateMean, final double sentRateMean) {
		final long currentTime = System.currentTimeMillis();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				droppedDataProvider.setCurrentYData(droppedRate, currentTime);
				droppedMeanDataProvider.setCurrentYData(droppedRateMean,
						currentTime);
				failedDataProvider.setCurrentYData(failedRate, currentTime);
				failedMeanDataProvider.setCurrentYData(failedRateMean,
						currentTime);
				receivedDataProvider.setCurrentYData(receivedRate, currentTime);
				receivedMeanDataProvider.setCurrentYData(receivedRateMean,
						currentTime);
				sentDataProvider.setCurrentYData(sentRate, currentTime);
				sentMeanDataProvider.setCurrentYData(sentRateMean, currentTime);
				sentDataStatistics.addValue(sentRate);

				if (sentPredictedDataProvider != null) {
					sentPredictedDataProvider.setCurrentYData(sentRate,
							currentTime);
					for (int i = 1; i < sentPredictBufferSize; i++) {
						final long time = currentTime + (i * 500);
						sentPredictedDataProvider.setCurrentYData(
								sentDataStatistics.getMean(), time);
					}
				}

				if (singleSamplingSelfAdaptivityManager != null
						&& singleSamplingSelfAdaptivityManager.isEnabled()) {
					sendLimitDataProvider.setCurrentYData(
							singleSamplingSelfAdaptivityManager
									.getMaxSendRate(), currentTime);
					lowerSendLimitDataProvider.setCurrentYData(
							singleSamplingSelfAdaptivityManager
									.getMaxSendRateLowerBound(), currentTime);
					theoreticSampledSentRateDataProvider.setCurrentYData(
							singleSamplingSelfAdaptivityManager
									.getTheoreticSampledSentRate(), currentTime);
					theoreticUnsampledSentRateDataProvider.setCurrentYData(
							singleSamplingSelfAdaptivityManager
									.getTheoreticUnsampledSentRate(),
							currentTime);
				} else {
					sendLimitDataProvider.setCurrentYData(0, currentTime);
					lowerSendLimitDataProvider.setCurrentYData(0, currentTime);
					theoreticSampledSentRateDataProvider.setCurrentYData(0,
							currentTime);
					theoreticUnsampledSentRateDataProvider.setCurrentYData(0,
							currentTime);
				}
			}
		});
	}

	private Trace addTrace(XYGraph statsXyGraph,
			CircularBufferDataProvider dataProvider, String name,
			int traceColor, TraceType traceType, PointStyle pointStyle) {
		Trace trace = new Trace(name, statsXyGraph.primaryXAxis,
				statsXyGraph.primaryYAxis, dataProvider);
		trace.setTraceColor(Display.getDefault().getSystemColor(traceColor));
		trace.setPointStyle(pointStyle);
		trace.setPointSize(8);
		trace.setTraceType(traceType);
		statsXyGraph.addTrace(trace);
		return trace;
	}

	public void setSingleSamplingSelfAdaptivityManager(
			SingleSensorSamplingSelfAdaptivityManager singleSamplingSelfAdaptivityManager) {
		this.singleSamplingSelfAdaptivityManager = singleSamplingSelfAdaptivityManager;
	}
}
