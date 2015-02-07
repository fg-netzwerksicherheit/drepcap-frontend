/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.merger;

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

import drepcap.frontend.conf.DefaultValues;
import drepcap.frontend.self_adaptivity.analyzer.single.MaxSendRateCalculationApproach;
import drepcap.frontend.self_adaptivity.manager.cooperative.CooperationSelfAdaptivityManager;

import org.eclipse.swt.events.SelectionAdapter;

/**
 * 
 * UI component for configuring cooperative self-adaptivity.
 * 
 * @author Ruediger Gad
 *
 */
public class CooperativeSelfAdaptivityControlGroup extends Group {

	private Button btnGranularityOverrideStatus;
	private Spinner spinnerGranularity;
	private Label granularityLabel;

	private boolean selfAdaptivityEnabled = false;
	private Button btnToggleSelfAdaptivityEnabled;

	private CooperationSelfAdaptivityManager cooperationSelfAdaptivityManager;

	public CooperativeSelfAdaptivityControlGroup(Composite parent, int style,
			CooperationSelfAdaptivityManager mgr) {
		super(parent, style);

		cooperationSelfAdaptivityManager = mgr;
		
		setLayout(new GridLayout(4, true));
		setText("Self-Adaptivity Control");

		Label lblStrategy = new Label(this, SWT.NONE);
		lblStrategy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblStrategy.setText("Strategy");

		Combo comboSelfAdaptationStrategy = new Combo(this, SWT.NONE);
		comboSelfAdaptationStrategy.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 3, 1));

		Label lblMaxsendStrat = new Label(this, SWT.NONE);
		lblMaxsendStrat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblMaxsendStrat.setText("MSR Sel. Strat.");

		Combo comboMaxSendRateCalculationStrategy = new Combo(this, SWT.NONE);
		comboMaxSendRateCalculationStrategy.setLayoutData(new GridData(
				SWT.FILL, SWT.CENTER, true, false, 3, 1));
		for (MaxSendRateCalculationApproach ma : MaxSendRateCalculationApproach
				.values()) {
			comboMaxSendRateCalculationStrategy.add(ma.name());
		}
		comboMaxSendRateCalculationStrategy.select(0);
		comboMaxSendRateCalculationStrategy.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//FIXME: Handle selection.
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnGranularityOverrideStatus = new Button(this, SWT.CHECK);
		btnGranularityOverrideStatus.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		btnGranularityOverrideStatus.setText("Gran. OR");
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

		btnToggleSelfAdaptivityEnabled = new Button(this, SWT.NONE);
		btnToggleSelfAdaptivityEnabled
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleSelfAdaptivityEnabledState();
					}
				});
		btnToggleSelfAdaptivityEnabled.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 4, 1));
		btnToggleSelfAdaptivityEnabled.setText("Enable Self-adaptivity");
		updateGranularityLabelTxt();
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
			// FIXME: Set granularity in self-adaptivity manager.
			updateGranularityLabelTxt();
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected void toggleSelfAdaptivityEnabledState() {
		if (selfAdaptivityEnabled) {
			cooperationSelfAdaptivityManager.disable();
			selfAdaptivityEnabled = false;
			btnToggleSelfAdaptivityEnabled.setText("Enable Self-adaptivity");
		} else {
			cooperationSelfAdaptivityManager.enable();
			selfAdaptivityEnabled = true;
			btnToggleSelfAdaptivityEnabled.setText("Disable Self-adaptivity");
		}
	}

}
