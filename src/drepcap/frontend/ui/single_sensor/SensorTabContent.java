/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.single_sensor;

import java.io.FileInputStream;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.jms.receiver.ByteArrayReceiver;
import drepcap.frontend.jms.receiver.StatsDataReceiver;
import drepcap.frontend.jms.receiver.StringReceiver;
import drepcap.frontend.ui.CmdComposite;
import drepcap.frontend.ui.TabWithJmsConnection;
import drepcap.frontend.util.FileSystemHelper;

/**
 * 
 * UI component for controlling a single sensor.
 * 
 * @author Ruediger Gad
 *
 */
public class SensorTabContent extends TabWithJmsConnection {

	private StyledText monitorText;

	private JmsAdapter jmsSensorAdapter;
	private String sensorName;

	private boolean sensorColdStandby = false;
	private boolean sensorHotStandby = false;
	private boolean toFifoActive = false;

	private ToolItem tltmHotStandby;
	private ToolItem tltmToFifo;
	private Thread fifoOutThread;
	private String fifoName = "";
	private ToolItem tltmToggleSimpleSingleSelfAdaptivity;
	private SingleSensorStatsPlot singleSensorStatsPlot;

	private CmdComposite cmdComposite;

	private ToolItem tltmColdStandby;
	private SingleSensorSelfAdaptivityGroup groupSimpleSelfadaptivity;
	private Group groupCaptureOptions;
	private Text textCaptureInterface;
	private Text textCaptureFilter;

	private long lastActivationCommandSendTime = 0;
	private long lastActivationCommandSendTimeAdv = 0;
	private boolean advActRttActive = false;

	private ToolItem tltmToggleAdvancedActivationRtt;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SensorTabContent(Composite parent, int style, final String sensorName) {
		super(parent, style);
		this.sensorName = sensorName;
		setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		Composite toolsComposite = new Composite(sashForm, SWT.NONE);
		toolsComposite.setLayout(new FormLayout());

		ToolBar toolBar = new ToolBar(toolsComposite, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(100);
		fd_toolBar.top = new FormAttachment(0);
		fd_toolBar.left = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);

		tltmColdStandby = new ToolItem(toolBar, SWT.NONE);
		tltmColdStandby.setText("Activate (Cold)");
		tltmColdStandby.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleSensorColdStandby();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		tltmHotStandby = new ToolItem(toolBar, SWT.NONE);
		tltmHotStandby.setText("Activate (Hot)");
		tltmHotStandby.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleSensorHotStandby();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		tltmToFifo = new ToolItem(toolBar, SWT.NONE);
		tltmToFifo.setText("To fifo");
		tltmToFifo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleToFifoActive();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		tltmToggleSimpleSingleSelfAdaptivity = new ToolItem(toolBar, SWT.NONE);
		tltmToggleSimpleSingleSelfAdaptivity.setText("Enable Simple Adapt.");

		tltmToggleAdvancedActivationRtt = new ToolItem(toolBar, SWT.NONE);
		tltmToggleAdvancedActivationRtt.setText("Enable Adv. Act. RTT");
		tltmToggleAdvancedActivationRtt
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleAdvancedActivationRttMeasurement();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		ScrolledComposite optionsScrolledComposite = new ScrolledComposite(
				toolsComposite, SWT.V_SCROLL);
		optionsScrolledComposite.setExpandVertical(true);
		optionsScrolledComposite.setExpandHorizontal(true);
		Composite optionsComposite = new Composite(optionsScrolledComposite,
				SWT.NONE);
		optionsScrolledComposite.setContent(optionsComposite);
		FormData fd_optionsScrolledComposite = new FormData();
		fd_optionsScrolledComposite.left = new FormAttachment(0);
		fd_optionsScrolledComposite.bottom = new FormAttachment(100);
		fd_optionsScrolledComposite.top = new FormAttachment(toolBar);
		fd_optionsScrolledComposite.right = new FormAttachment(100);
		optionsScrolledComposite.setLayoutData(fd_optionsScrolledComposite);
		optionsComposite.setLayout(new GridLayout(1, true));

		groupCaptureOptions = new Group(optionsComposite, SWT.NONE);
		groupCaptureOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		groupCaptureOptions.setText("Capture Options");
		groupCaptureOptions.setLayout(new GridLayout(4, true));

		Label lblNewLabel = new Label(groupCaptureOptions, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblNewLabel.setBounds(0, 0, 65, 15);
		lblNewLabel.setText("Interface");

		textCaptureInterface = new Text(groupCaptureOptions, SWT.BORDER);
		textCaptureInterface.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		textCaptureInterface.setText("lo");
		textCaptureInterface.setBounds(0, 0, 79, 21);

		Label lblNewLabel_1 = new Label(groupCaptureOptions, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblNewLabel_1.setBounds(0, 0, 65, 15);
		lblNewLabel_1.setText("Filter");

		textCaptureFilter = new Text(groupCaptureOptions, SWT.BORDER);
		textCaptureFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		textCaptureFilter.setText("less 1");
		textCaptureFilter.setBounds(0, 0, 79, 21);

		groupSimpleSelfadaptivity = new SingleSensorSelfAdaptivityGroup(
				optionsComposite, SWT.NONE, this);
		groupSimpleSelfadaptivity.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		groupSimpleSelfadaptivity
				.setSwitchToolItem(tltmToggleSimpleSingleSelfAdaptivity);

		optionsScrolledComposite.setMinSize(optionsComposite.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));

		tltmToggleSimpleSingleSelfAdaptivity
				.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						groupSimpleSelfadaptivity
								.toggleSimpleSingleSelfAdaptivity();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

		singleSensorStatsPlot = new SingleSensorStatsPlot(sashForm, SWT.NONE);
		singleSensorStatsPlot.setLayout(new FillLayout(SWT.HORIZONTAL));

		monitorText = new StyledText(sashForm, SWT.BORDER);
		monitorText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				monitorText.setTopIndex(monitorText.getLineCount() - 1);
			}
		});

		cmdComposite = new CmdComposite(sashForm, SWT.NONE);
		sashForm.setWeights(new int[] { 86, 124, 29, 52 });
	}

	public void connectToJmsSensor(Connection connection) throws JMSException {
		jmsSensorAdapter = new JmsAdapter(connection, sensorName);

		cmdComposite.setSensorTab(this);
		cmdComposite.connectJmsSensorAdapter(jmsSensorAdapter);

		jmsSensorAdapter.clearMonitorReceivers();
		jmsSensorAdapter.addMonitorReceiver(new StringReceiver() {
			@Override
			public void process(final String str) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						monitorText.append(str + "\n");
					}
				});
			}
		});

		jmsSensorAdapter.clearStatsDataReceivers();
		jmsSensorAdapter.addStatsDataReceiver(new StatsDataReceiver() {

			@Override
			public void processSingleSensorStatsData(double droppedRate,
					double failedRate, double receivedRate, double sentRate,
					double droppedRateMean, double failedRateMean,
					double receivedRateMean, double sentRateMean) {
				if (singleSensorStatsPlot != null) {
					singleSensorStatsPlot.updateData(droppedRate, failedRate,
							receivedRate, sentRate, droppedRateMean,
							failedRateMean, receivedRateMean, sentRateMean);
				}
			}
		});

		jmsSensorAdapter.sendCommand("get-standby-states");

		groupSimpleSelfadaptivity.connectJmsAdapter(jmsSensorAdapter);
		singleSensorStatsPlot
				.setSingleSamplingSelfAdaptivityManager(groupSimpleSelfadaptivity
						.getSelfAdaptivityManager());
	}

	private void toggleSensorColdStandby() {
		if (sensorColdStandby) {
			try {
				lastActivationCommandSendTime = System.nanoTime();
				lastActivationCommandSendTimeAdv = System.nanoTime();
				jmsSensorAdapter.sendCommand("activate-from-cold-standby "
						+ textCaptureInterface.getText() + " "
						+ textCaptureFilter.getText());
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				jmsSensorAdapter.sendCommand("cold-standby");
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setColdStandbyStatus(boolean s) {
		sensorColdStandby = s;
		if (!sensorColdStandby) {
			printActivationRtt();
			tltmColdStandby.setText("Cold Standby");
		} else {
			tltmColdStandby.setText("Activate (Cold)");
		}
	}

	private void toggleSensorHotStandby() {
		if (sensorHotStandby) {
			try {
				lastActivationCommandSendTime = System.nanoTime();
				lastActivationCommandSendTimeAdv = System.nanoTime();
				jmsSensorAdapter.sendCommand("activate-from-hot-standby");
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				jmsSensorAdapter.sendCommand("hot-standby");
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setHotStandbyStatus(boolean s) {
		sensorHotStandby = s;
		if (!sensorHotStandby) {
			printActivationRtt();
			tltmHotStandby.setText("Hot Standby");
		} else {
			tltmHotStandby.setText("Activate (Hot)");
		}
	}

	private void toggleToFifoActive() {
		if (!toFifoActive) {
			fifoName = sensorName + ".fifo";
			fifoOutThread = new Thread(new Runnable() {
				public void run() {
					try {
						jmsSensorAdapter
								.startPcapByteArrayToFifoOutput(fifoName);
					} catch (IOException | JMSException ex) {
						ex.printStackTrace();
					}
				}
			});
			fifoOutThread.start();
			tltmToFifo.setText("Stop fifo");
			toFifoActive = !toFifoActive;
		} else {
			if (fifoOutThread.isAlive()) {
				try {
					// Dummy read to unblock a potentially blocked
					// fifoOutThread.
					FileInputStream fin = new FileInputStream(fifoName);
					fin.read();
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fifoOutThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				jmsSensorAdapter.stopReceiveData();
				FileSystemHelper.rmFile(sensorName + ".fifo");
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
			toFifoActive = !toFifoActive;
			tltmToFifo.setText("To fifo");
		}
	}

	private void printActivationRtt() {
		long timeTmp = System.nanoTime();
		if (lastActivationCommandSendTime != 0) {
			System.out.println("Activation RTT: "
					+ ((timeTmp - lastActivationCommandSendTime) / 1000000.0));
			lastActivationCommandSendTime = 0;
		}
	}

	private void toggleAdvancedActivationRttMeasurement() {
		if (!advActRttActive) {
			tltmToggleAdvancedActivationRtt.setText("Disable Adv. Act. RTT");

			jmsSensorAdapter.clearByteArrayDataReceivers();
			jmsSensorAdapter.addByteArrayDataReceiver(new ByteArrayReceiver() {
				@Override
				public void process(byte[] receivedData) {
					long timeTmp = System.nanoTime();
					if (lastActivationCommandSendTimeAdv != 0) {
						System.out
								.println("Advanced Activation RTT: "
										+ ((timeTmp - lastActivationCommandSendTimeAdv) / 1000000.0));
						lastActivationCommandSendTimeAdv = 0;
					}
				}
			});
			try {
				jmsSensorAdapter.startReceiveData();
			} catch (JMSException e) {
				e.printStackTrace();
			}

			advActRttActive = true;
		} else {
			tltmToggleAdvancedActivationRtt.setText("Enable Adv. Act. RTT");

			try {
				jmsSensorAdapter.stopReceiveData();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			jmsSensorAdapter.clearByteArrayDataReceivers();

			advActRttActive = false;
		}
	}
}
