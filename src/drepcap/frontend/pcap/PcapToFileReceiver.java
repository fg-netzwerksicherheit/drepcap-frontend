/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.pcap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 
 * Abstract class for writing data into a file that is prefixed with a pcap-file header.
 * 
 * @author Ruediger Gad
 *
 */
public abstract class PcapToFileReceiver {

	protected FileOutputStream out;
	protected String fileName;

	protected PcapToFileReceiver(String fileName) throws IOException {
		this.fileName = fileName;
		init();
	}

	protected void init() throws FileNotFoundException, IOException {
		out = new FileOutputStream(fileName);

		ByteBuffer pcapFileHeader = ByteBuffer.allocate(24);
		pcapFileHeader.putInt(0xa1b2c3d4);
		pcapFileHeader.putInt(0x00020004);
		pcapFileHeader.putInt(0);
		pcapFileHeader.putInt(0);
		pcapFileHeader.putInt(0xffff);
		pcapFileHeader.putInt(1);

		out.write(pcapFileHeader.array());
	}

	public void finished() {
		try {
			out.close();
			out = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void writePcapData(byte[] pcapData, int offset, int length) {
		if (out == null) {
			return;
		}
		
		try {
			out.write(pcapData, offset, length);
		} catch (IOException e) {
			if (e.getMessage().contains("Broken pipe")) {
				System.out.println("We have a broken pipe. "
						+ "This could be due to the reading process being stopped. "
						+ "Trying to re-open the pipe.");
				finished();
				try {
					init();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				e.printStackTrace();
			}
		}
	}
}
