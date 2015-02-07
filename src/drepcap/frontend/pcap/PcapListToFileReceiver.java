/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.pcap;

import java.io.IOException;
import java.util.List;

import drepcap.frontend.jms.receiver.ObjectReceiver;

/**
 * 
 * Class to receive data as a list of byte arrays and forwards the data to a file with a pcap-file header.
 * 
 * @author Ruediger Gad
 *
 */
public class PcapListToFileReceiver extends PcapToFileReceiver implements
		ObjectReceiver {

	public PcapListToFileReceiver(String fileName) throws IOException {
		super(fileName);
	}

	@Override
	public void process(Object receivedObject) {
		if (receivedObject != null && receivedObject instanceof List) {
			List<?> list = (List<?>) receivedObject;

			if (list.size() > 0 && list.get(0) instanceof byte[]) {
				for (Object pcapDataObj : list) {
					byte[] pcapData = (byte[]) pcapDataObj;
					writePcapData(pcapData, 0, pcapData.length);
				}
			}
		}
	}

}
