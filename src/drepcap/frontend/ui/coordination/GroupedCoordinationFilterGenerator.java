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
 * Coordination filter generation strategy for grouped aggregation; i.e., the
 * data that shall be captured at each sensor is grouped in blocks.
 * 
 * @author Ruediger Gad
 * 
 */
public class GroupedCoordinationFilterGenerator implements CoordinationFilterGenerationStrategy {

    @Override
    public int getToMinimum(int fromValue) {
        return fromValue;
    }

    @Override
    public int getFromMaximum(int denominator) {
        return denominator - 1;
    }

    @Override
    public int getToMaximum(int denominator) {
        return denominator - 1;
    }

    @Override
    public boolean checkValueValidity(int from, int to, int denominator) {
        return (denominator > 1 && from <= to && to < denominator);
    }

    @Override
    public String generateFilter(int index, int from, int to, int denominator) {
        return "(ip [4:2] & " + (denominator - 1) + " >= " + from + ") and (ip [4:2] & " + (denominator - 1) + " <= " + to + ")";
    }

    @Override
    public String getFromText() {
        return "From";
    }

    @Override
    public String getToText() {
        return "To";
    }

}
