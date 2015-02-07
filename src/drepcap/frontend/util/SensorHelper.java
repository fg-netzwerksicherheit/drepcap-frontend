/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.util;

/**
 * 
 * Helper class for handling sensors.
 * 
 * @author Ruediger Gad
 *
 */
public class SensorHelper {

	public static String getSensorIdFrom(String fullSensorName) {
		return fullSensorName.split("\\.")[3];
	}	
	
}
