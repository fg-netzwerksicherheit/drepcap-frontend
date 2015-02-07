/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.manager.single;

/**
 * 
 * Interface for a single sensor self-adaptivity manager.
 * 
 * @author Ruediger Gad
 *
 */
public interface SingleSelfAdaptivityManager {

	public void addPerformanceData(double droppedRate, double failedRate,
			double receivedRate, double sentRate);

}
