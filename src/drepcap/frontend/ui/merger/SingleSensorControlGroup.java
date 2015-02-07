/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.merger;

import java.util.Map;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;

import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.ui.CmdComposite;
import drepcap.frontend.ui.coordination.CoordinationFilterGenerationStrategy;
import drepcap.frontend.ui.coordination.CoordinationStrategy;
import drepcap.frontend.ui.coordination.GroupedCoordinationFilterGenerator;
import drepcap.frontend.ui.coordination.UniformCoordinationFilterGenerator;

/**
 * 
 * UI component for configuring a single sensor that is part of a cooperation group.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorControlGroup extends Group {

	private CoordinationFilterGenerationStrategy coordinationFilterGenerationStrategy;

	public SingleSensorControlGroup(Composite parent, int style,
			final Table selectedSensorsTable,
			final CmdComposite sensorCmdComposite, final ToolItem tltmRemove,
			final TableColumn tblclmnCaptureRatioFrom,
			final TableColumn tblclmnCaptureRatioTo,
			final Map<?, ?> sensorAdapters) {
		super(parent, style);
		setText("Single Sensor Control");
		setLayout(new GridLayout(4, true));

		coordinationFilterGenerationStrategy = new GroupedCoordinationFilterGenerator();

		Label lblCoordinationStrategy = new Label(this, SWT.NONE);
		lblCoordinationStrategy.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		lblCoordinationStrategy.setSize(103, 15);
		lblCoordinationStrategy.setText("Coord. Strat.");

		final Combo comboCoordinationStrategy = new Combo(this, SWT.NONE);
		comboCoordinationStrategy.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 3, 1));
		comboCoordinationStrategy.setSize(319, 21);
		comboCoordinationStrategy.select(0);

		Label lblDenexp = new Label(this, SWT.NONE);
		lblDenexp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		lblDenexp.setSize(103, 15);
		lblDenexp.setText("Den.Exp.");

		final Spinner denominatorExponentSpinner = new Spinner(this, SWT.BORDER);
		denominatorExponentSpinner.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		denominatorExponentSpinner.setSize(103, 21);
		denominatorExponentSpinner.setMaximum(16);
		denominatorExponentSpinner.setMinimum(1);

		Label lblDen = new Label(this, SWT.NONE);
		lblDen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		lblDen.setSize(103, 15);
		lblDen.setText("Denominator");

		final Label lblDenominator = new Label(this, SWT.NONE);
		lblDenominator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblDenominator.setSize(103, 15);
		lblDenominator.setText("2");

		final Label lblFrom = new Label(this, SWT.NONE);
		lblFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		lblFrom.setSize(103, 15);
		lblFrom.setText("From");

		final Spinner spinnerFrom = new Spinner(this, SWT.BORDER);
		spinnerFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		spinnerFrom.setSize(103, 21);

		final Label lblTo = new Label(this, SWT.NONE);
		lblTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		lblTo.setSize(103, 15);
		lblTo.setText("To");

		final Spinner spinnerTo = new Spinner(this, SWT.BORDER);
		spinnerTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		spinnerTo.setSize(103, 21);

		for (CoordinationStrategy cs : CoordinationStrategy.values()) {
			comboCoordinationStrategy.add(cs.toString());
		}
		comboCoordinationStrategy.select(0);

		selectedSensorsTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sensorCmdComposite.disconnect();
				spinnerFrom.setMinimum(0);
				spinnerFrom.setMaximum(coordinationFilterGenerationStrategy
						.getFromMaximum((int) Math.pow(2, 16)));
				spinnerTo.setMaximum(coordinationFilterGenerationStrategy
						.getToMaximum((int) Math.pow(2, 16)));

				TableItem[] selection = selectedSensorsTable.getSelection();
				if (selection != null && selection.length == 1) {
					tltmRemove.setEnabled(true);

					TableItem selectedItem = selection[0];
					String sensorId = selectedItem.getText(0);
					if (sensorCmdComposite != null && sensorId != null
							&& sensorAdapters.containsKey(sensorId)
							&& sensorAdapters.get(sensorId) != null) {
						sensorCmdComposite
								.connectJmsSensorAdapter((JmsAdapter) sensorAdapters
										.get(sensorId));
						sensorCmdComposite.setOutputPrefix(sensorId);

						spinnerFrom.setSelection(Integer.parseInt(selectedItem
								.getText(1)));
						spinnerTo
								.setMinimum(coordinationFilterGenerationStrategy
										.getToMinimum(spinnerFrom
												.getSelection()));
						spinnerTo.setSelection(Integer.parseInt(selectedItem
								.getText(2)));

						int denominator = Integer.parseInt(selectedItem
								.getText(3));
						if (denominator > 0) {
							// FIXME: Beware that the below calculation may
							// result in undesired results due to round-off
							// errors. This was briefly tested for exponents
							// from 1 to 16 but this is no guarantee that things
							// may not break. For this prototype, right now, we
							// go with this suboptimal solution.
							denominatorExponentSpinner.setSelection((int) (Math
									.log(denominator) / Math.log(2)));
							lblDenominator.setText("" + denominator);
							spinnerTo
									.setMaximum(coordinationFilterGenerationStrategy
											.getToMaximum(denominator));
						} else {
							spinnerTo
									.setMaximum(coordinationFilterGenerationStrategy
											.getToMaximum(Integer
													.parseInt(lblDenominator
															.getText())));
						}
					}
				} else {
					tltmRemove.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button btnTotable = new Button(this, SWT.NONE);
		btnTotable.setSize(103, 24);
		btnTotable.setText("ToTable");

		Button btnApplyselected = new Button(this, SWT.NONE);
		btnApplyselected.setSize(103, 24);
		btnApplyselected.setText("ApplySelected");

		btnApplyselected.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = selectedSensorsTable.getSelection();
				if (selection != null && selection.length == 1) {
					TableItem selectedItem = selection[0];
					if (selectedItem != null) {
						String sensorId = selectedItem.getText(0);

						JmsAdapter sensorAdapter = (JmsAdapter) sensorAdapters
								.get(sensorId);

						int index = selectedSensorsTable.getSelectionIndex();
						int from = Integer.parseInt(selectedItem.getText(1));
						int to = Integer.parseInt(selectedItem.getText(2));
						int denominator = Integer.parseInt(selectedItem
								.getText(3));

						if (sensorAdapter != null
								&& coordinationFilterGenerationStrategy
										.checkValueValidity(from, to,
												denominator)) {
							try {
								sensorAdapter.sendCommand("remove-all-filters");
								// FIXME: Don't use sleep here.
								Thread.sleep(500);
								sensorAdapter.sendCommand("add-filter "
										+ coordinationFilterGenerationStrategy
												.generateFilter(index, from,
														to, denominator));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnTotable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = selectedSensorsTable.getSelection();
				if (selection != null && selection.length == 1) {
					TableItem selectedItem = selection[0];

					if (selectedItem != null
							&& coordinationFilterGenerationStrategy.checkValueValidity(
									spinnerFrom.getSelection(),
									spinnerTo.getSelection(),
									Integer.parseInt(lblDenominator.getText()))) {
						selectedItem.setText(1, "" + spinnerFrom.getSelection());
						selectedItem.setText(2, "" + spinnerTo.getSelection());
						selectedItem.setText(3, lblDenominator.getText());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		spinnerFrom.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				spinnerTo.setMinimum(coordinationFilterGenerationStrategy
						.getToMinimum(spinnerFrom.getSelection()));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		denominatorExponentSpinner
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int denominator = (int) Math.pow(2,
								denominatorExponentSpinner.getSelection());
						lblDenominator.setText("" + denominator);
						spinnerFrom
								.setMaximum(coordinationFilterGenerationStrategy
										.getFromMaximum(denominator));
						spinnerTo
								.setMaximum(coordinationFilterGenerationStrategy
										.getToMaximum(denominator));
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		comboCoordinationStrategy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (CoordinationStrategy.valueOf(comboCoordinationStrategy
						.getText())) {
				case GroupedCoordinationFilterGenerator:
					coordinationFilterGenerationStrategy = new GroupedCoordinationFilterGenerator();
					break;
				case UniformCoordinationFilterGenerator:
					coordinationFilterGenerationStrategy = new UniformCoordinationFilterGenerator();
					break;
				default:
					coordinationFilterGenerationStrategy = new GroupedCoordinationFilterGenerator();
					break;
				}

				tblclmnCaptureRatioFrom
						.setText(coordinationFilterGenerationStrategy
								.getFromText());
				lblFrom.setText(coordinationFilterGenerationStrategy
						.getFromText());

				tblclmnCaptureRatioTo
						.setText(coordinationFilterGenerationStrategy
								.getToText());
				lblTo.setText(coordinationFilterGenerationStrategy.getToText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
