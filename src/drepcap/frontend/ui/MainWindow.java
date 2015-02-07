/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui;

import javax.jms.JMSException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import drepcap.frontend.jms.JmsConnection;
import drepcap.frontend.jms.SensorNameReceiver;
import drepcap.frontend.ui.merger.PacketMergerTabContent;
import drepcap.frontend.ui.single_sensor.SensorTabContent;

/**
 * 
 * The main window of the GUI frontend.
 * 
 * @author Ruediger Gad
 *
 */
public class MainWindow {

	private Button connectButton;
	protected Shell shell;
	private Text brokerUrlText;
	private List sensorList;
	private CTabFolder sensorTabFolder;

	private JmsConnection jmsConnection;
	private ToolItem tltmOpen;
	private ToolItem tltmRefresh;

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1100, 600);
		shell.setText("Distributed Remote Packet Capturing (DRePCap) Frontend");
		shell.setLayout(new FormLayout());
		shell.addShellListener(new ShellListener() {
			@Override
			public void shellIconified(ShellEvent e) {
			}

			@Override
			public void shellDeiconified(ShellEvent e) {
			}

			@Override
			public void shellDeactivated(ShellEvent e) {
			}

			@Override
			public void shellClosed(ShellEvent e) {
				if (jmsConnection != null) {
					jmsConnection.disconnect();
				}
			}

			@Override
			public void shellActivated(ShellEvent e) {
			}
		});

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		fd_composite.right = new FormAttachment(100);
		fd_composite.bottom = new FormAttachment(0, 30);
		composite.setLayoutData(fd_composite);

		Label lblBrokerurl = new Label(composite, SWT.NONE);
		FormData fd_lblBrokerurl = new FormData();
		fd_lblBrokerurl.right = new FormAttachment(0, 86);
		fd_lblBrokerurl.top = new FormAttachment(0, 8);
		fd_lblBrokerurl.left = new FormAttachment(0, 10);
		lblBrokerurl.setLayoutData(fd_lblBrokerurl);
		lblBrokerurl.setText("BrokerURL");

		brokerUrlText = new Text(composite, SWT.BORDER);
		brokerUrlText.setText("tcp://127.0.0.1:61616");
		FormData fd_brokerUrlText = new FormData();
		fd_brokerUrlText.left = new FormAttachment(lblBrokerurl, 6);
		fd_brokerUrlText.top = new FormAttachment(0, 4);
		brokerUrlText.setLayoutData(fd_brokerUrlText);

		connectButton = new Button(composite, SWT.NONE);
		fd_brokerUrlText.right = new FormAttachment(100, -102);
		FormData fd_connectButton = new FormData();
		fd_connectButton.top = new FormAttachment(0, 2);
		fd_connectButton.left = new FormAttachment(brokerUrlText, 6);
		fd_connectButton.right = new FormAttachment(100, -10);
		connectButton.setLayoutData(fd_connectButton);
		connectButton.setText("Connect");
		connectButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent ev) {
			}

			@Override
			public void mouseDown(MouseEvent ev) {
				toggleConnect();
			}

			@Override
			public void mouseDoubleClick(MouseEvent ev) {
			}
		});

		SashForm mainSashForm = new SashForm(shell, SWT.NONE);
		FormData fd_mainSashForm = new FormData();
		fd_mainSashForm.top = new FormAttachment(composite);
		fd_mainSashForm.bottom = new FormAttachment(100);
		fd_mainSashForm.left = new FormAttachment(0);
		fd_mainSashForm.right = new FormAttachment(100);
		mainSashForm.setLayoutData(fd_mainSashForm);

		SashForm leftSashForm = new SashForm(mainSashForm, SWT.VERTICAL);

		ToolBar toolBar = new ToolBar(leftSashForm, SWT.FLAT | SWT.RIGHT);

		tltmOpen = new ToolItem(toolBar, SWT.NONE);
		tltmOpen.setToolTipText("Open the selected entity.");
		tltmOpen.setText("Open");
		tltmOpen.setEnabled(false);

		tltmRefresh = new ToolItem(toolBar, SWT.NONE);
		tltmRefresh.setToolTipText("Refresh the list of available components.");
		tltmRefresh.setText("Refresh");
		tltmRefresh.setEnabled(false);
		tltmRefresh.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (jmsConnection != null) {
					try {
						jmsConnection.getDestinations();
					} catch (JMSException ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		tltmOpen.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openSensorTab();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		sensorList = new List(leftSashForm, SWT.BORDER | SWT.FULL_SELECTION);
		sensorList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selection = sensorList.getSelection();
				if (selection != null && selection.length == 1
						&& selection[0] != null
						&& selection[0].compareTo("") != 0
						&& !tabNameExists(selection[0], sensorTabFolder)) {
					tltmOpen.setEnabled(true);
				} else {
					tltmOpen.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		sensorList.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSensorTab();
			}
		});
		leftSashForm.setWeights(new int[] { 1, 1 });

		SashForm rightSashForm = new SashForm(mainSashForm, SWT.VERTICAL);

		sensorTabFolder = new CTabFolder(rightSashForm, SWT.BORDER);
		sensorTabFolder.setSelectionBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		rightSashForm.setWeights(new int[] { 225 });
		leftSashForm.setWeights(new int[] { 33, 250 });
		mainSashForm.setWeights(new int[] {141, 950});
	}

	private void toggleConnect() {
		if (connectButton.getText() == "Connect") {
			jmsConnection = new JmsConnection();
			jmsConnection.setSensorNameReceiver(new SensorNameReceiver() {
				@Override
				public void sensorNamesReceived(
						final java.util.List<String> sensorNameList) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							sensorList.removeAll();
							for (String sensorName : sensorNameList) {
								sensorList.add(sensorName);
							}

							for (CTabItem tabItem : sensorTabFolder.getItems()) {
								Control contr = tabItem.getControl();
								if (contr instanceof PacketMergerTabContent) {
									((PacketMergerTabContent) contr)
											.updateAvailableSensors();
								}
							}
						}
					});
				}
			});
			jmsConnection.connect(brokerUrlText.getText());
			connectButton.setText("Disconnect");
			tltmRefresh.setEnabled(true);
		} else {
			jmsConnection.disconnect();
			connectButton.setText("Connect");
			tltmRefresh.setEnabled(false);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					sensorList.removeAll();

				}
			});
		}
	}

	private void openSensorTab() {
		if (sensorList.getSelectionCount() == 1) {
			String selectedSensorName = sensorList.getSelection()[0];

			if (!tabNameExists(selectedSensorName, sensorTabFolder)) {
				CTabItem sensorTabItem = new CTabItem(sensorTabFolder,
						SWT.CLOSE);
				sensorTabItem.setText(selectedSensorName);

				TabWithJmsConnection tabContent;
				if (selectedSensorName.contains(".single.")) {
					tabContent = new SensorTabContent(sensorTabFolder,
							SWT.NONE, selectedSensorName);
				} else if (selectedSensorName.contains(".merged.")) {
					tabContent = new PacketMergerTabContent(sensorTabFolder,
							SWT.NONE, selectedSensorName, this);
				} else {
					return;
				}

				try {
					tabContent
							.connectToJmsSensor(jmsConnection.getConnection());
				} catch (JMSException e) {
					e.printStackTrace();
				}
				sensorTabItem.setControl(tabContent);

				sensorTabFolder
						.setSelection(sensorTabFolder.getItemCount() - 1);
				;
				sensorTabFolder.showSelection();
				tltmOpen.setEnabled(false);
			}
		}
	}

	private boolean tabNameExists(String name, CTabFolder tabFolder) {
		CTabItem[] tabItems = tabFolder.getItems();

		for (int i = 0; i < tabItems.length; i++) {
			if (tabItems[i].getText().compareTo(name) == 0) {
				return true;
			}
		}

		return false;
	}

	public String[] getSensors() {
		return sensorList.getItems();
	}
}
