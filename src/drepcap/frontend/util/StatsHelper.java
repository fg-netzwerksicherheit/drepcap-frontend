/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.util;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * 
 * Helper class for handling stats data.
 * 
 * @author Ruediger Gad
 *
 */
public class StatsHelper {

	public static double getValueAndAddToStats(Map<?, ?> statsDataMap, String key,
			DescriptiveStatistics stats) {
		double ret = 0;
		
		final Object obj = statsDataMap.get(key);
		
		if (obj != null && obj instanceof Double) {
			ret = ((Double) obj).doubleValue();
		}
		
		if (stats != null) {
			stats.addValue(ret);
		}
		
		return ret;
	}
}
