/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.jms.receiver;

/**
 * 
 * Interface for receiving data as byte array.
 * 
 * @author Ruediger Gad
 *
 */
public interface ByteArrayReceiver {
	
	public void process(byte[] receivedData);

}
