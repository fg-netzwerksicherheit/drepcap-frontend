/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.frontend.ui.coordination;

/**
 * 
 * Interface for filter generation strategies in coordinated setups.
 * 
 * @author Ruediger Gad
 *
 */
public interface CoordinationFilterGenerationStrategy {

	int getToMinimum(int fromValue);

	int getFromMaximum(int denominator);

	int getToMaximum(int denominator);

	boolean checkValueValidity(int from, int to, int denominator);

	String generateFilter(int index, int from, int to, int denominator);

	String getFromText();

	String getToText();

}
