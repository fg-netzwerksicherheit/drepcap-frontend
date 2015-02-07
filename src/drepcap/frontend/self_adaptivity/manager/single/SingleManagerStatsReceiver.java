/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.self_adaptivity.manager.single;

import clojure.lang.Ratio;

/**
 * 
 * Interface for receiving sensor stats in a single sensor self-adaptivity scenario.
 * 
 * @author Ruediger Gad
 *
 */
public interface SingleManagerStatsReceiver {

	public void statsUpdated(double maxSendRate, double theoreticCaptureRatio,
			Ratio actualCaptureRatio, Ratio lowerFilterOffset);
}
