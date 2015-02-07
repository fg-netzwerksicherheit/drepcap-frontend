/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.merger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import clojure.lang.Ratio;
import drepcap.frontend.jms.JmsAdapter;
import drepcap.frontend.jms.receiver.StringReceiver;
import drepcap.frontend.self_adaptivity.manager.cooperative.CooperationSelfAdaptivityManager;
import drepcap.frontend.self_adaptivity.manager.cooperative.CooperationStatsUpdateListener;
import drepcap.frontend.ui.CmdComposite;
import drepcap.frontend.ui.MainWindow;
import drepcap.frontend.ui.TabWithJmsConnection;
import drepcap.frontend.util.FileSystemHelper;
import drepcap.frontend.util.SensorHelper;

/**
 * 
 * UI component for configuring a packet merger.
 * 
 * @author Ruediger Gad
 *
 */
public class PacketMergerTabContent extends TabWithJmsConnection {

    private JmsAdapter mergerJmsAdapter;
    private String packetMergerName;

    private boolean toFifoActive = false;

    private ToolItem tltmToFifo;
    private Thread fifoOutThread;
    private String fifoName = "";

    private CmdComposite cmdComposite;

    private StyledText monitorText;
    private Table selectedSensorsTable;

    private MainWindow mainWindow;
    private List availableSensorsList;
    private ToolItem tltmAdd;

    private Map<String, JmsAdapter> sensorJmsAdapters = new HashMap<>();
    private CmdComposite sensorCmdComposite;

    private CooperationSelfAdaptivityManager cooperationSelfAdaptivityManager = new CooperationSelfAdaptivityManager();

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public PacketMergerTabContent(Composite parent, int style, final String packetMergerName, MainWindow mainWindow) {
        super(parent, style);
        this.packetMergerName = packetMergerName;
        this.mainWindow = mainWindow;

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

        ScrolledComposite optionsScrolledComposite = new ScrolledComposite(toolsComposite, SWT.V_SCROLL);
        optionsScrolledComposite.setExpandVertical(true);
        optionsScrolledComposite.setExpandHorizontal(true);
        SashForm optionsSashForm = new SashForm(optionsScrolledComposite, SWT.NONE);
        optionsSashForm.setLayout(new GridLayout(3, true));

        Composite availableSensorsComposite = new Composite(optionsSashForm, SWT.NONE);
        availableSensorsComposite.setLayout(new FormLayout());
        availableSensorsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        ToolBar availableSensorToolBar = new ToolBar(availableSensorsComposite, SWT.FLAT | SWT.RIGHT);
        FormData fd_availableSensorToolBar = new FormData();
        fd_availableSensorToolBar.top = new FormAttachment(0);
        fd_availableSensorToolBar.left = new FormAttachment(0);
        fd_availableSensorToolBar.bottom = new FormAttachment(10);
        fd_availableSensorToolBar.right = new FormAttachment(100);
        availableSensorToolBar.setLayoutData(fd_availableSensorToolBar);

        tltmAdd = new ToolItem(availableSensorToolBar, SWT.NONE);
        tltmAdd.setText("Add");
        tltmAdd.setEnabled(false);
        tltmAdd.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addSensor();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        availableSensorsList = new List(availableSensorsComposite, SWT.BORDER);
        FormData fd_availableSensorsList = new FormData();
        fd_availableSensorsList.bottom = new FormAttachment(100);
        fd_availableSensorsList.top = new FormAttachment(availableSensorToolBar);
        fd_availableSensorsList.right = new FormAttachment(100);
        fd_availableSensorsList.left = new FormAttachment(0);
        availableSensorsList.setLayoutData(fd_availableSensorsList);
        availableSensorsList.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String[] selection = availableSensorsList.getSelection();
                if (selection != null && selection.length == 1 && !sensorSelected(SensorHelper.getSensorIdFrom(selection[0]))) {
                    tltmAdd.setEnabled(true);
                } else {
                    tltmAdd.setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Composite selectedSensorsComposite = new Composite(optionsSashForm, SWT.NONE);
        selectedSensorsComposite.setLayout(new FormLayout());
        selectedSensorsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        ToolBar selectedSensorToolBar = new ToolBar(selectedSensorsComposite, SWT.FLAT | SWT.RIGHT);
        FormData fd_selectedSensorToolBar = new FormData();
        fd_selectedSensorToolBar.top = new FormAttachment(0);
        fd_selectedSensorToolBar.left = new FormAttachment(0);
        fd_selectedSensorToolBar.bottom = new FormAttachment(10);
        fd_selectedSensorToolBar.right = new FormAttachment(100);
        selectedSensorToolBar.setLayoutData(fd_selectedSensorToolBar);

        final ToolItem tltmRemove = new ToolItem(selectedSensorToolBar, SWT.NONE);
        tltmRemove.setText("Remove");
        tltmRemove.setEnabled(false);
        tltmRemove.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSensor();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        selectedSensorsTable = new Table(selectedSensorsComposite, SWT.BORDER | SWT.FULL_SELECTION);
        FormData fd_selectedSensorsTable = new FormData();
        fd_selectedSensorsTable.top = new FormAttachment(10);
        fd_selectedSensorsTable.bottom = new FormAttachment(100);
        fd_selectedSensorsTable.right = new FormAttachment(100);
        fd_selectedSensorsTable.left = new FormAttachment(0);
        selectedSensorsTable.setLayoutData(fd_selectedSensorsTable);
        selectedSensorsTable.setHeaderVisible(true);
        selectedSensorsTable.setLinesVisible(true);

        TableColumn tblclmnId = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnId.setWidth(30);
        tblclmnId.setText("Id");

        final TableColumn tblclmnCaptureRatioFrom = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnCaptureRatioFrom.setWidth(40);
        tblclmnCaptureRatioFrom.setText("From");

        final TableColumn tblclmnCaptureRatioTo = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnCaptureRatioTo.setWidth(40);
        tblclmnCaptureRatioTo.setText("To");

        TableColumn tblclmnCaptureRatioDenominator = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnCaptureRatioDenominator.setWidth(40);
        tblclmnCaptureRatioDenominator.setText("Den.");

        TableColumn tblclmnMaximumSendRate = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnMaximumSendRate.setWidth(60);
        tblclmnMaximumSendRate.setText("MSR");

        TableColumn tblclmnTheoreticCaptureRatio = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnTheoreticCaptureRatio.setWidth(40);
        tblclmnTheoreticCaptureRatio.setText("TCR");

        TableColumn tblclmnActualCaptureRatio = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnActualCaptureRatio.setWidth(40);
        tblclmnActualCaptureRatio.setText("ACR");

        TableColumn tblclmnLowerFilterOffset = new TableColumn(selectedSensorsTable, SWT.NONE);
        tblclmnLowerFilterOffset.setWidth(40);
        tblclmnLowerFilterOffset.setText("Off.");

        optionsScrolledComposite.setContent(optionsSashForm);
        FormData fd_optionsScrolledComposite = new FormData();
        fd_optionsScrolledComposite.left = new FormAttachment(0);
        fd_optionsScrolledComposite.bottom = new FormAttachment(100);
        fd_optionsScrolledComposite.top = new FormAttachment(toolBar);
        fd_optionsScrolledComposite.right = new FormAttachment(100);
        optionsScrolledComposite.setLayoutData(fd_optionsScrolledComposite);

        SashForm sensorControlSashForm = new SashForm(optionsSashForm, SWT.VERTICAL);

        ScrolledComposite sensorControlScrolledComposite = new ScrolledComposite(sensorControlSashForm, SWT.V_SCROLL);
        sensorControlScrolledComposite.setExpandVertical(true);
        sensorControlScrolledComposite.setExpandHorizontal(true);
        Composite sensorControlComposite = new Composite(sensorControlScrolledComposite, SWT.NONE);
        sensorControlScrolledComposite.setContent(sensorControlComposite);
        sensorControlComposite.setLayout(new GridLayout(1, true));

        sensorCmdComposite = new CmdComposite(sensorControlSashForm, SWT.NONE);

        SingleSensorControlGroup grpSingleSensorControl = new SingleSensorControlGroup(sensorControlComposite, SWT.NONE,
                selectedSensorsTable, sensorCmdComposite, tltmRemove, tblclmnCaptureRatioFrom, tblclmnCaptureRatioTo, sensorJmsAdapters);
        grpSingleSensorControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        CooperativeSelfAdaptivityControlGroup grpSelfadaptivityControl = new CooperativeSelfAdaptivityControlGroup(sensorControlComposite,
                SWT.NONE, cooperationSelfAdaptivityManager);
        grpSelfadaptivityControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        monitorText = new StyledText(sashForm, SWT.BORDER);
        monitorText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                monitorText.setTopIndex(monitorText.getLineCount() - 1);
            }
        });

        cmdComposite = new CmdComposite(sashForm, SWT.NONE);
        sashForm.setWeights(new int[] { 236, 20, 38 });

        updateAvailableSensors();

        sensorControlScrolledComposite.setMinSize(sensorControlComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sensorControlSashForm.setWeights(new int[] { 147, 61 });
        optionsSashForm.setWeights(new int[] { 58, 174, 212 });

        cooperationSelfAdaptivityManager.addStatusUpdateListener(new CooperationStatsUpdateListener() {
            @Override
            public void statusUpdated(final String sensorId, final double maxSendRate, final double theoreticCaptureRatio,
                    final Ratio actualCaptureRatio, final Ratio lowerFilterOffset) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < selectedSensorsTable.getItemCount(); i++) {
                            final TableItem itm = selectedSensorsTable.getItem(i);

                            if (itm.getText(0).compareTo(sensorId) == 0) {
                                itm.setText(1, lowerFilterOffset.numerator.toString());
                                if (actualCaptureRatio.doubleValue() == 1.0 && lowerFilterOffset.doubleValue() != 0.0) {
                                	itm.setText(2, "" + (lowerFilterOffset.denominator.intValue() - 1));
                                } else {
                                	itm.setText(2, "" + (lowerFilterOffset.numerator.intValue() + actualCaptureRatio.numerator.intValue()));	
                                }
                                
                                itm.setText(3, actualCaptureRatio.denominator.toString());
                                itm.setText(4, "" + maxSendRate);
                                itm.setText(5, "" + theoreticCaptureRatio);
                                itm.setText(6, actualCaptureRatio.toString());
                                itm.setText(7, lowerFilterOffset.toString());
                            }
                        }
                    }
                });
            }
        });
    }

    protected void addSensor() {
        String[] selection = availableSensorsList.getSelection();
        if (selection != null && selection.length == 1 && selection[0] != null) {
            String sensorId = SensorHelper.getSensorIdFrom(selection[0]);
            if (!sensorSelected(sensorId)) {
                try {
                    mergerJmsAdapter.sendCommand("add-sensor " + sensorId);

                    JmsAdapter sJmsAdapter = new JmsAdapter(mergerJmsAdapter.getConnection(), selection[0]);
                    sensorJmsAdapters.put(sensorId, sJmsAdapter);
                    cooperationSelfAdaptivityManager.addSensor(sensorId, sJmsAdapter);

                    TableItem tblItm = new TableItem(selectedSensorsTable, SWT.NONE);
                    tblItm.setText(0, sensorId);
                    tblItm.setText(1, "0");
                    tblItm.setText(2, "0");
                    tblItm.setText(3, "0");
                    tblItm.setText(4, "-1");
                    if (selectedSensorsTable.getItemCount() <= 1) {
                        tblItm.setText(5, "1");
                        tblItm.setText(6, "1");
                    } else {
                        tblItm.setText(5, "0");
                        tblItm.setText(6, "0");
                    }
                    tblItm.setText(7, "0");
                    tltmAdd.setEnabled(false);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void removeSensor() {
        TableItem[] selection = selectedSensorsTable.getSelection();
        if (selection != null && selection.length == 1) {
            try {
                String sensorId = selection[0].getText(0);

                mergerJmsAdapter.sendCommand("remove-sensor " + sensorId);

                sensorCmdComposite.disconnect();

                JmsAdapter adapter = sensorJmsAdapters.get(sensorId);
                adapter.disconnect();
                sensorJmsAdapters.remove(sensorId);
                cooperationSelfAdaptivityManager.removeSensor(sensorId);

                for (int i = 0; i < selectedSensorsTable.getItemCount(); i++) {
                    if (selection[0] == selectedSensorsTable.getItem(i)) {
                        selectedSensorsTable.remove(i);
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sensorSelected(String sensorId) {
        for (TableItem tblItm : selectedSensorsTable.getItems()) {
            if (tblItm.getText(0).compareTo(sensorId) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void connectToJmsSensor(Connection connection) throws JMSException {
        mergerJmsAdapter = new JmsAdapter(connection, packetMergerName);

        cmdComposite.connectJmsSensorAdapter(mergerJmsAdapter);

        mergerJmsAdapter.clearMonitorReceivers();
        mergerJmsAdapter.addMonitorReceiver(new StringReceiver() {
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
    }

    private void toggleToFifoActive() {
        if (!toFifoActive) {
            fifoName = packetMergerName + ".fifo";
            fifoOutThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        mergerJmsAdapter.startPcapByteArrayToFifoOutput(fifoName);
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
                mergerJmsAdapter.stopReceiveData();
                FileSystemHelper.rmFile(packetMergerName + ".fifo");
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            toFifoActive = !toFifoActive;
            tltmToFifo.setText("To fifo");
        }
    }

    public void updateAvailableSensors() {
        availableSensorsList.removeAll();
        String[] sensors = mainWindow.getSensors();
        for (String s : sensors) {
            if (s.startsWith("pcap.single.")) {
                availableSensorsList.add(s);
            }
        }
    }
}
