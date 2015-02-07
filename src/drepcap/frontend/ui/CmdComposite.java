/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.jms.receiver.StringReceiver;
import drepcap.frontend.ui.single_sensor.SensorTabContent;

/**
 * 
 * UI component for displaying text output and performing text input.
 * This is primarily intended to be used as a sort of interactive &quot;shell&quot;.
 * 
 * @author Ruediger Gad
 *
 */
public class CmdComposite extends Composite {

	private Text cmdInput;
	private StyledText cmdOutput;
	private SensorTabContent sensorTab;
	private JmsAdapter jmsSensorAdapter;
	private KeyListener keyListener;

	private int completionIndex = 0;
	private List<String> completionHistory = new ArrayList<>();
	
	private String outputPrefix = ""; 

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public CmdComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new FormLayout());

		cmdInput = new Text(this, SWT.BORDER);
		FormData fd_cmdInput = new FormData();
		fd_cmdInput.bottom = new FormAttachment(100);
		fd_cmdInput.top = new FormAttachment(100, -24);
		fd_cmdInput.left = new FormAttachment(0);
		fd_cmdInput.right = new FormAttachment(100);
		cmdInput.setLayoutData(fd_cmdInput);

		cmdOutput = new StyledText(this, SWT.BORDER);
		cmdOutput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				cmdOutput.setTopIndex(cmdOutput.getLineCount() - 1);
			}
		});
		FormData fd_cmdOutput = new FormData();
		fd_cmdOutput.bottom = new FormAttachment(cmdInput, 0);
		fd_cmdOutput.top = new FormAttachment(0, 0);
		fd_cmdOutput.left = new FormAttachment(0);
		fd_cmdOutput.right = new FormAttachment(100);
		cmdOutput.setLayoutData(fd_cmdOutput);

		keyListener = new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.CR:
					final String txt = cmdInput.getText();

					try {
						jmsSensorAdapter.sendCommand(txt);
					} catch (JMSException ex) {
						ex.printStackTrace();
					}

					if (completionHistory.contains(txt)) {
						completionHistory.remove(txt);
					}
					completionHistory.add(txt);
					completionIndex = completionHistory.size();

					cmdOutput.append(txt + "\n");
					cmdInput.setText("");
					break;
				case SWT.ARROW_UP:
					if (completionIndex > 0) {
						completionIndex--;
						cmdInput.setText(completionHistory.get(completionIndex));
						cmdInput.setSelection(cmdInput.getCharCount());
					}
					break;
				case SWT.ARROW_DOWN:
					if (completionIndex < (completionHistory.size() - 1)) {
						completionIndex++;
						cmdInput.setText(completionHistory.get(completionIndex));
					}
					break;
				}
			}
		};

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void connectJmsSensorAdapter(final JmsAdapter jmsSensorAdapter) {
		this.jmsSensorAdapter = jmsSensorAdapter;

		initCompletionHistory();

		jmsSensorAdapter.clearCommandReplyReceivers();
		jmsSensorAdapter.addCommandReplyReceiver(new StringReceiver() {
			@Override
			public void process(final String str) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						cmdOutput.append(outputPrefix + "> " + str + "\n");
						if (sensorTab != null) {
							switch (str) {
							case "cold-standby true":
								sensorTab.setColdStandbyStatus(true);
								break;
							case "cold-standby false":
								sensorTab.setColdStandbyStatus(false);
								break;
							case "hot-standby true":
								sensorTab.setHotStandbyStatus(true);
								break;
							case "hot-standby false":
								sensorTab.setHotStandbyStatus(false);
								break;
							default:
								break;
							}
						}
					}
				});
			}
		});

		cmdInput.addKeyListener(keyListener);
	}

	public void setSensorTab(SensorTabContent sensorTab) {
		this.sensorTab = sensorTab;
	}

	public void disconnect() {
		if (jmsSensorAdapter != null) {
			try {
				jmsSensorAdapter.stopReceiveData();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			jmsSensorAdapter.clearByteArrayDataReceivers();
			jmsSensorAdapter.clearCommandReplyReceivers();
			jmsSensorAdapter.clearMonitorReceivers();
			jmsSensorAdapter.clearObjectReceivers();
		}
		cmdInput.removeKeyListener(keyListener);
	}

	private void initCompletionHistory() {
		completionHistory.clear();
		if (jmsSensorAdapter.getComponentName().contains("single")) {
			completionHistory.add("get-silent");
			completionHistory.add("set-silent true");
			completionHistory.add("get-offset");
			completionHistory.add("set-offset 123");
			completionHistory
					.add("add-filter tcp[tcpflags] & (tcp-syn|tcp-ack|tcp-fin) != 0");
			completionHistory.add("replace-filter less 1 with-filter less 2");
			completionHistory.add("add-filter less 1");
			completionHistory.add("remove-last-filter");
			completionHistory.add("cold-standby");
			completionHistory.add("hot-standby");
			completionHistory.add("activate-from-cold-standby lo");
			completionHistory.add("activate-from-cold-standby lo less 1");
			completionHistory.add("activate-from-hot-standby");
			completionHistory.add("get-standby-states");
			completionHistory.add("get-filters");
		}
		if (jmsSensorAdapter.getComponentName().contains("merged")) {
			completionHistory.add("add-sensor 1");
			completionHistory.add("remove-sensor 1");
			completionHistory.add("get-sensors");
		}
		completionIndex = completionHistory.size();
	}
	
	public String getOutputPrefix() {
		return outputPrefix;
	}
	
	public void setOutputPrefix(String prefix) {
		outputPrefix = prefix;
	}

}
