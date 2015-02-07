/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.conf;

/**
 * 
 * Currently hard-coded definition of default values.
 * 
 * @author Ruediger Gad
 *
 */
public class DefaultValues {
	public static final int DEFAULT_MAX_SEND_RATE_DELTA = 1;
	public static final int DEFAULT_STATS_WINDOW = 5;
	public static final int DEFAULT_POST_COMMAND_INACTIVITY = DEFAULT_STATS_WINDOW - 1;
	public static final int DEFAULT_LOWER_BOUND = 10;
	public static final int DEFAULT_GRANULARITY_EXPONENT = 6;
}
