/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.single_sensor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolItem;

import drepcap.frontend.conf.DefaultValues;
import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.self_adaptivity.analyzer.single.MaxSendRateCalculationApproach;
import drepcap.frontend.self_adaptivity.manager.single.SingleSensorSamplingSelfAdaptivityManager;

/**
 * 
 * UI component for configuring self-adaptivity of a single sensor.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorSelfAdaptivityGroup extends Group {

	private Spinner spinnerGranularity;
	private Label granularityLabel;
	private Spinner spinnerThresholdValue;
	private Combo comboMaxSendRateCalculationApproach;
	private Button btnThresholdOverrideStatus;
	private Button btnDataStatsWindowOverrideStatus;
	private Spinner spinnerDataStatsWindowSize;
	private Button btnMaxSendRateDeltaOverrideStatus;
	private Spinner spinnerMaxSendRateDelta;
	private Button btnPostCommandInactivityOverrideStatus;
	private Spinner spinnerPostCommandInactivity;
	private Button btnLowerBoundOverrideStatus;
	private Spinner spinnerLowerBound;
	private Button btnGranularityOverrideStatus;

	private SingleSensorSamplingSelfAdaptivityManager singleSamplingSelfAdaptivityManager;
	private ToolItem tltmToggleSimpleSingleSelfAdaptivity;

	private JmsAdapter jmsSensorAdapter;

	public SingleSensorSelfAdaptivityGroup(Composite parent, int style,
			SensorTabContent sensorTabContent) {
		super(parent, style);
		setLayout(new GridLayout(4, false));
		setText("Simple Self-adaptivity");

		Label lblMaxSendRateCalculationApproach = new Label(this, SWT.NONE);
		lblMaxSendRateCalculationApproach.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		lblMaxSendRateCalculationApproach.setBounds(0, 0, 87, 48);
		lblMaxSendRateCalculationApproach.setText("Max. Send Rate Calculation");

		comboMaxSendRateCalculationApproach = new Combo(this, SWT.NONE);
		comboMaxSendRateCalculationApproach.setLayoutData(new GridData(
				SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboMaxSendRateCalculationApproach.setBounds(0, 0, 108, 48);
		for (MaxSendRateCalculationApproach ma : MaxSendRateCalculationApproach
				.values()) {
			comboMaxSendRateCalculationApproach.add(ma.name());
		}
		comboMaxSendRateCalculationApproach.select(0);

		btnThresholdOverrideStatus = new Button(this, SWT.CHECK);
		btnThresholdOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnThresholdOverrideStatus.setSize(144, 48);
		btnThresholdOverrideStatus.setText("Threshold Override");

		spinnerThresholdValue = new Spinner(this, SWT.BORDER);
		spinnerThresholdValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		spinnerThresholdValue.setSize(216, 48);

		btnDataStatsWindowOverrideStatus = new Button(this, SWT.CHECK);
		btnDataStatsWindowOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnDataStatsWindowOverrideStatus.setSize(240, 48);
		btnDataStatsWindowOverrideStatus.setText("Data Stats. Win. Override");
		btnDataStatsWindowOverrideStatus
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setDataWindowSizes();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		spinnerDataStatsWindowSize = new Spinner(this, SWT.BORDER);
		spinnerDataStatsWindowSize.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		spinnerDataStatsWindowSize.setSize(216, 48);
		spinnerDataStatsWindowSize.setMinimum(1);
		spinnerDataStatsWindowSize
				.setSelection(DefaultValues.DEFAULT_STATS_WINDOW);
		spinnerDataStatsWindowSize.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setDataWindowSizes();
			}
		});

		btnMaxSendRateDeltaOverrideStatus = new Button(this, SWT.CHECK);
		btnMaxSendRateDeltaOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnMaxSendRateDeltaOverrideStatus.setSize(240, 48);
		btnMaxSendRateDeltaOverrideStatus.setText("MSR Delta Override [%]");
		btnMaxSendRateDeltaOverrideStatus
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setMaxSendRateDelta();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		spinnerMaxSendRateDelta = new Spinner(this, SWT.BORDER);
		spinnerMaxSendRateDelta.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		spinnerMaxSendRateDelta.setSize(216, 48);
		spinnerMaxSendRateDelta.setMinimum(1);
		spinnerMaxSendRateDelta
				.setSelection(DefaultValues.DEFAULT_MAX_SEND_RATE_DELTA);
		spinnerMaxSendRateDelta.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setMaxSendRateDelta();
			}
		});

		btnPostCommandInactivityOverrideStatus = new Button(this, SWT.CHECK);
		btnPostCommandInactivityOverrideStatus.setLayoutData(new GridData(
				SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnPostCommandInactivityOverrideStatus.setSize(240, 48);
		btnPostCommandInactivityOverrideStatus.setText("Post Cmd. Inact.");
		btnPostCommandInactivityOverrideStatus
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setPostCommandInactivity();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		spinnerPostCommandInactivity = new Spinner(this, SWT.BORDER);
		spinnerPostCommandInactivity.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		spinnerPostCommandInactivity.setSize(216, 48);
		spinnerPostCommandInactivity.setMinimum(0);
		spinnerPostCommandInactivity
				.setSelection(DefaultValues.DEFAULT_POST_COMMAND_INACTIVITY);
		spinnerPostCommandInactivity.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPostCommandInactivity();
			}
		});

		btnLowerBoundOverrideStatus = new Button(this, SWT.CHECK);
		btnLowerBoundOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnLowerBoundOverrideStatus.setSize(240, 48);
		btnLowerBoundOverrideStatus.setText("Lower Bound Override [%]");
		btnLowerBoundOverrideStatus
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setLowerBound();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		spinnerLowerBound = new Spinner(this, SWT.BORDER);
		spinnerLowerBound.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		spinnerLowerBound.setSize(216, 48);
		spinnerLowerBound.setMinimum(1);
		spinnerLowerBound.setSelection(DefaultValues.DEFAULT_LOWER_BOUND);
		spinnerLowerBound.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setLowerBound();
			}
		});

		btnGranularityOverrideStatus = new Button(this, SWT.CHECK);
		btnGranularityOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnGranularityOverrideStatus.setSize(240, 48);
		btnGranularityOverrideStatus.setText("Granularity Override [2^x]");
		btnGranularityOverrideStatus
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setGranularity();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		spinnerGranularity = new Spinner(this, SWT.BORDER);
		spinnerGranularity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		spinnerGranularity.setSize(216, 48);
		spinnerGranularity.setMinimum(1);
		spinnerGranularity.setMaximum(16);
		spinnerGranularity
				.setSelection(DefaultValues.DEFAULT_GRANULARITY_EXPONENT);
		spinnerGranularity.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setGranularity();
			}
		});

		granularityLabel = new Label(this, SWT.NONE);
		granularityLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		granularityLabel.setBounds(0, 0, 400, 15);
		new Label(this, SWT.NONE);
		updateGranularityLabelTxt();

		spinnerThresholdValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (btnThresholdOverrideStatus.getSelection()) {
					singleSamplingSelfAdaptivityManager
							.setMaxSendThreshold(spinnerThresholdValue
									.getSelection());
				}
			}
		});

	}

	private void updateGranularityLabelTxt() {
		granularityLabel.setText("Granularity: 2^"
				+ spinnerGranularity.getSelection() + " = "
				+ (int) Math.pow(2, spinnerGranularity.getSelection()));
	}

	protected void setGranularity() {
		if (btnGranularityOverrideStatus.getSelection()
				&& spinnerGranularity.getSelection() >= 1
				&& spinnerGranularity.getSelection() <= 16) {
			singleSamplingSelfAdaptivityManager
					.setGranularityFromExponent(spinnerGranularity
							.getSelection());
			updateGranularityLabelTxt();
		}
	}

	protected void setLowerBound() {
		if (btnLowerBoundOverrideStatus.getSelection()
				&& spinnerLowerBound.getSelection() > 0) {
			singleSamplingSelfAdaptivityManager.setLowerBound(spinnerLowerBound
					.getSelection());
		}
	}

	protected void setPostCommandInactivity() {
		if (btnPostCommandInactivityOverrideStatus.getSelection()
				&& spinnerPostCommandInactivity.getSelection() >= 0) {
			singleSamplingSelfAdaptivityManager
					.setPostCommandInactivity(spinnerPostCommandInactivity
							.getSelection());
		}
	}

	protected void setDataWindowSizes() {
		if (btnDataStatsWindowOverrideStatus.getSelection()
				&& spinnerDataStatsWindowSize.getSelection() > 0) {
			int windowSize = spinnerDataStatsWindowSize.getSelection();
			jmsSensorAdapter.setStatsWindowSize(windowSize);
		}
	}

	protected void setMaxSendRateDelta() {
		if (btnMaxSendRateDeltaOverrideStatus.getSelection()
				&& spinnerMaxSendRateDelta.getSelection() > 0
				&& singleSamplingSelfAdaptivityManager != null
				&& singleSamplingSelfAdaptivityManager.isEnabled()) {
			singleSamplingSelfAdaptivityManager
					.setMaxSendRateDelta(spinnerMaxSendRateDelta.getSelection());
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected void connectJmsAdapter(JmsAdapter jmsSensorAdapter) {
		this.jmsSensorAdapter = jmsSensorAdapter;
		singleSamplingSelfAdaptivityManager = new SingleSensorSamplingSelfAdaptivityManager(
				jmsSensorAdapter);
	}

	public void toggleSimpleSingleSelfAdaptivity() {
		if (!singleSamplingSelfAdaptivityManager.isEnabled()) {
			System.out
					.println("Enabling singleSamplingSelfAdaptivityManager...");

			singleSamplingSelfAdaptivityManager
					.setMaxSendRateCalculator(MaxSendRateCalculationApproach
							.valueOf(comboMaxSendRateCalculationApproach
									.getText()));
			singleSamplingSelfAdaptivityManager.enable();
			tltmToggleSimpleSingleSelfAdaptivity
					.setText("Disable Simple Adapt.");
			if (btnThresholdOverrideStatus.getSelection()) {
				singleSamplingSelfAdaptivityManager
						.setMaxSendThreshold(spinnerThresholdValue
								.getSelection());
			} else {
				spinnerThresholdValue
						.setSelection((int) singleSamplingSelfAdaptivityManager
								.getMaxSendRateThreshold());
			}

			if (btnLowerBoundOverrideStatus.getSelection()) {
				singleSamplingSelfAdaptivityManager
						.setLowerBoundThreshold(spinnerLowerBound
								.getSelection() / 100.0);
			} else {
				spinnerLowerBound
						.setSelection((int) (singleSamplingSelfAdaptivityManager
								.getLowerBoundThreshold() * 100));
			}

			if (btnGranularityOverrideStatus.getSelection()) {
				singleSamplingSelfAdaptivityManager
						.setGranularityFromExponent(spinnerGranularity
								.getSelection());
			}
		} else {
			System.out
					.println("Disabling singleSamplingSelfAdaptivityManager...");

			singleSamplingSelfAdaptivityManager.disable();
			tltmToggleSimpleSingleSelfAdaptivity
					.setText("Enable Simple Adapt.");
		}
	}

	public void setSwitchToolItem(ToolItem tltmToggleSimpleSingleSelfAdaptivity) {
		this.tltmToggleSimpleSingleSelfAdaptivity = tltmToggleSimpleSingleSelfAdaptivity;
	}

	public SingleSensorSamplingSelfAdaptivityManager getSelfAdaptivityManager() {
		return singleSamplingSelfAdaptivityManager;
	}

}
