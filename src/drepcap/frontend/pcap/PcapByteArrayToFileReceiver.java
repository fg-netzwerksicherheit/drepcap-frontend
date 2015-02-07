/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.pcap;

import java.io.IOException;

import drepcap.frontend.jms.receiver.ByteArrayReceiver;

/**
 * 
 * Class that receives data as byte array and forwards the data to a file with a pcap-file header.
 * 
 * @author Ruediger Gad
 *
 */
public class PcapByteArrayToFileReceiver extends PcapToFileReceiver implements
		ByteArrayReceiver {

	public PcapByteArrayToFileReceiver(String fileName) throws IOException {
		super(fileName);
	}

	@Override
	public void process(byte[] receivedData) {
		writePcapData(receivedData, 0, receivedData.length);
	}

}
