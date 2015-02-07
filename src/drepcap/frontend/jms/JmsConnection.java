/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 
 * Facade that encapsulates the &quot;low-level&quot; JMS connection handling.
 * 
 * @author Ruediger Gad
 *
 */
public class JmsConnection {

	private MessageConsumer brokerInfoConsumer;
	private MessageProducer brokerInfoProducer;
	private Topic brokerInfoTopic;
	private Connection connection;

	private SensorNameReceiver sensorNameReceiver;
	private Session session;

	public void connect(String url) {
		System.out.println("Connecting to JMS broker: " + url);

		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					url);
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			brokerInfoTopic = session.createTopic("broker.info");
			brokerInfoConsumer = session.createConsumer(brokerInfoTopic);
			brokerInfoProducer = session.createProducer(brokerInfoTopic);

			brokerInfoConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					System.out.println("Got message:" + msg);

					if (msg instanceof TextMessage) {
						TextMessage textMsg = (TextMessage) msg;
						try {
							String txt = textMsg.getText();
							if (txt.startsWith("reply destinations ")) {
								String dstNamesString = txt.replaceFirst(
										"reply destinations ", "");
								System.out.println("Got destinations: "
										+ dstNamesString);

								List<String> sensorNames = new ArrayList<>();
								String[] splitDstNames = dstNamesString
										.split(" ");
								for (int i = 0; i < splitDstNames.length; i++) {
									String currentName = splitDstNames[i];
									if (currentName.endsWith(".data")
											&& !currentName
													.contains("ActiveMQ.Advisory")) {
										currentName = currentName.replaceAll(
												".data", "");
										currentName = currentName.replaceFirst(
												"/topic/", "");
										sensorNames.add(currentName);
									}
								}

								if (sensorNameReceiver != null) {
									sensorNameReceiver
											.sensorNamesReceived(sensorNames);
								}
							}
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				}
			});
			brokerInfoProducer.send(session
					.createTextMessage("command get-destinations"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void getDestinations() throws JMSException {
		brokerInfoProducer.send(session
				.createTextMessage("command get-destinations"));
	}

	public void disconnect() {
		System.out.println("Disconnecting from Broker.");

		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setSensorNameReceiver(SensorNameReceiver rcvr) {
		sensorNameReceiver = rcvr;
	}

}
