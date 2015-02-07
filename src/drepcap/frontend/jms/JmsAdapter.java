/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import clj_assorted_utils.JavaUtils;
import drepcap.frontend.conf.DefaultValues;
import drepcap.frontend.jms.receiver.ByteArrayReceiver;
import drepcap.frontend.jms.receiver.ObjectReceiver;
import drepcap.frontend.jms.receiver.StatsDataReceiver;
import drepcap.frontend.jms.receiver.StringReceiver;
import drepcap.frontend.pcap.PcapByteArrayToFileReceiver;
import drepcap.frontend.pcap.PcapListToFileReceiver;
import drepcap.frontend.pcap.PcapToFileReceiver;
import drepcap.frontend.util.FileSystemHelper;
import drepcap.frontend.util.StatsHelper;

/**
 * 
 * Adapter class that encapsulates the &quot;low-level&quot; JMS functionality.
 * 
 * @author Ruediger Gad
 * 
 */
public class JmsAdapter {

	private String componentName;

	private MessageConsumer commandConsumer;
	private MessageConsumer monitorConsumer;
	private Topic commandTopic;
	private MessageConsumer dataConsumer;
	private Topic dataTopic;
	private MessageProducer commandProducer;
	private Topic monitorTopic;
	private Session session;

	private List<ByteArrayReceiver> byteArrayDataReceivers = new ArrayList<>();
	private List<ObjectReceiver> objectReceivers = new ArrayList<>();
	private List<StringReceiver> commandReplyReceivers = new ArrayList<>();
	private List<StringReceiver> monitorReceivers = new ArrayList<>();
	private List<StatsDataReceiver> statsDataReceivers = new ArrayList<>();

	private Connection connection;

	private DescriptiveStatistics receivedStats = new DescriptiveStatistics(
			DefaultValues.DEFAULT_STATS_WINDOW);
	private DescriptiveStatistics sentStats = new DescriptiveStatistics(
			DefaultValues.DEFAULT_STATS_WINDOW);
	private DescriptiveStatistics droppedStats = new DescriptiveStatistics(
			DefaultValues.DEFAULT_STATS_WINDOW);
	private DescriptiveStatistics failedStats = new DescriptiveStatistics(
			DefaultValues.DEFAULT_STATS_WINDOW);

	/**
	 * 
	 * Components are typically pcap-sensors or packet-mergers. The component
	 * name is the name without any additional suffix, e.g.,
	 * "pcap.single.raw.2".
	 * 
	 * @param connection
	 * @param componentName
	 * @throws JMSException
	 */
	public JmsAdapter(Connection connection, String componentName)
			throws JMSException {
		this.componentName = componentName;
		this.connection = connection;
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		commandTopic = session.createTopic(componentName + ".command");
		commandConsumer = session.createConsumer(commandTopic);
		commandConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if (msg instanceof TextMessage) {
					TextMessage textMsg = (TextMessage) msg;
					try {
						final String txt = textMsg.getText();

						for (StringReceiver cmdReplyReceiver : commandReplyReceivers) {
							if (cmdReplyReceiver != null
									&& txt.startsWith("reply")) {
								cmdReplyReceiver.process(txt.replaceFirst(
										"reply ", ""));
							}
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});
		commandProducer = session.createProducer(commandTopic);

		monitorTopic = session.createTopic(componentName + ".monitor");
		monitorConsumer = session.createConsumer(monitorTopic);
		monitorConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if (msg instanceof TextMessage) {
					TextMessage textMsg = (TextMessage) msg;
					try {
						final String txt = textMsg.getText();

						for (StringReceiver monReceiver : monitorReceivers) {
							if (monReceiver != null) {
								monReceiver.process(txt);
							}
						}

						if (statsDataReceivers.size() > 0) {
							processStatsFromString(txt);
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void processStatsFromString(String statsString) {
		Map<?, ?> statsData = JavaUtils.readMapFromClojureString(statsString);
		if (statsData != null) {

			if (componentName.contains(".single.")) {
				Object relativeTotalObj = statsData.get("relative-total");
				if (relativeTotalObj != null && relativeTotalObj instanceof Map) {
					Map<?, ?> relativeTotalMap = (Map<?, ?>) relativeTotalObj;
					processSingleSensorRelativeTotalStatsMap(relativeTotalMap);
				}
			}
		}
	}

	private void processSingleSensorRelativeTotalStatsMap(Map<?, ?> statsData) {
		final double droppedRate = StatsHelper.getValueAndAddToStats(
				statsData, "dropped", droppedStats);
		final double droppedRateMean = droppedStats.getMean();

		final double failedRate = StatsHelper.getValueAndAddToStats(statsData,
				"failed", failedStats);
		final double failedRateMean = failedStats.getMean();

		final double receivedRate = StatsHelper.getValueAndAddToStats(
				statsData, "received", receivedStats);
		final double receivedRateMean = receivedStats.getMean();

		final double sentRate = StatsHelper.getValueAndAddToStats(statsData,
				"sent", sentStats);
		final double sentRateMean = sentStats.getMean();

		for (StatsDataReceiver statsDataReceiver : statsDataReceivers) {
			statsDataReceiver.processSingleSensorStatsData(droppedRate,
					failedRate, receivedRate, sentRate, droppedRateMean,
					failedRateMean, receivedRateMean, sentRateMean);
		}
	}
	
	public void setStatsWindowSize(int windowSize) {
		droppedStats.setWindowSize(windowSize);
		failedStats.setWindowSize(windowSize);
		receivedStats.setWindowSize(windowSize);
		sentStats.setWindowSize(windowSize);
	}

	public String getComponentName() {
		return componentName;
	}

	public void startReceiveData() throws JMSException {
		dataTopic = session.createTopic(componentName + ".data");
		dataConsumer = session.createConsumer(dataTopic);
		dataConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				for (ByteArrayReceiver baDataReceiver : byteArrayDataReceivers) {
					if (baDataReceiver != null && msg instanceof BytesMessage) {
						BytesMessage bMsg = (BytesMessage) msg;
						try {
							byte[] data = new byte[(int) bMsg.getBodyLength()];
							bMsg.readBytes(data);
							baDataReceiver.process(data);
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				}

				for (ObjectReceiver objReceiver : objectReceivers) {
					if (objReceiver != null && msg instanceof ObjectMessage) {
						ObjectMessage objMsg = (ObjectMessage) msg;
						try {
							objReceiver.process(objMsg.getObject());
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public void stopReceiveData() throws JMSException {
		if (dataConsumer != null) {
			dataConsumer.close();
			dataConsumer = null;
		}

		for (ByteArrayReceiver baDataReceiver : byteArrayDataReceivers) {
			if (baDataReceiver != null
					&& baDataReceiver instanceof PcapToFileReceiver) {
				((PcapToFileReceiver) baDataReceiver).finished();
			}
		}

		for (ObjectReceiver objReceiver : objectReceivers) {
			if (objReceiver != null
					&& objReceiver instanceof PcapToFileReceiver) {
				((PcapToFileReceiver) objReceiver).finished();
			}
		}
	}

	public void addByteArrayDataReceiver(ByteArrayReceiver rcvr) {
		byteArrayDataReceivers.add(rcvr);
	}

	public void clearByteArrayDataReceivers() {
		byteArrayDataReceivers.clear();
	}

	public void addCommandReplyReceiver(StringReceiver rcvr) {
		commandReplyReceivers.add(rcvr);
	}

	public void clearCommandReplyReceivers() {
		commandReplyReceivers.clear();
	}

	public void addMonitorReceiver(StringReceiver rcvr) {
		monitorReceivers.add(rcvr);
	}

	public void clearMonitorReceivers() {
		monitorReceivers.clear();
	}

	public void addObjectReceiver(ObjectReceiver rcvr) {
		objectReceivers.add(rcvr);
	}

	public void clearObjectReceivers() {
		objectReceivers.clear();
	}

	public void addStatsDataReceiver(StatsDataReceiver rcvr) {
		statsDataReceivers.add(rcvr);
	}

	public void clearStatsDataReceivers() {
		statsDataReceivers.clear();
	}

	public void sendCommand(String txt) throws JMSException {
		commandProducer.send(session.createTextMessage("command " + txt));
	}

	public void startPcapByteArrayToFifoOutput(String fifoPath)
			throws IOException, JMSException {
		FileSystemHelper.rmFile(fifoPath);
		if (FileSystemHelper.mkfifo(fifoPath) == 0) {
			clearByteArrayDataReceivers();
			addByteArrayDataReceiver(new PcapByteArrayToFileReceiver(fifoPath));
			startReceiveData();
		}
	}

	public void startPcapListToFifoOutput(String fifoPath) throws IOException,
			JMSException {
		FileSystemHelper.rmFile(fifoPath);
		if (FileSystemHelper.mkfifo(fifoPath) == 0) {
			clearObjectReceivers();
			addObjectReceiver(new PcapListToFileReceiver(fifoPath));
			startReceiveData();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void disconnect() throws JMSException {
		if (commandConsumer != null) {
			commandConsumer.close();
		}
		if (monitorConsumer != null) {
			monitorConsumer.close();
		}
		if (dataConsumer != null) {
			dataConsumer.close();
		}
		if (commandProducer != null) {
			commandProducer.close();
		}
		if (session != null) {
			session.close();
		}
	}
}
