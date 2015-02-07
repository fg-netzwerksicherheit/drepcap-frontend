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
 * Class for generating filters for selecting "interleaved" non-overlapping sets of packets. 
 * <br><br>
 * Examples for generated filters:<br>
 * Den.Exp. = 3; Numerator = 4; Modulus = 0<br>
 * > pcap-filters ["(ip [4:2] & 7 == 0) or (ip [4:2] & 7 == 2) or (ip [4:2] & 7 == 4) or (ip [4:2] & 7 == 6)"]<br>
 * Den.Exp. = 3; Numerator = 4; Modulus = 1<br>
 * > pcap-filters ["(ip [4:2] & 7 == 1) or (ip [4:2] & 7 == 3) or (ip [4:2] & 7 == 5) or (ip [4:2] & 7 == 7)"]<br>
 * <br><br>
 * Den.Exp. = 3; Numerator = 2; Modulus = 0<br>
 * > pcap-filters ["(ip [4:2] & 7 == 0) or (ip [4:2] & 7 == 4)"]<br>
 * Den.Exp. = 3; Numerator = 2; Modulus = 1<br>
 * > pcap-filters ["(ip [4:2] & 7 == 1) or (ip [4:2] & 7 == 5)"]<br>
 * ...<br>
 * 
 * @author Ruediger Gad
 *
 */
public class UniformCoordinationFilterGenerator implements
		CoordinationFilterGenerationStrategy {

	@Override
	public int getToMinimum(int fromValue) {
		return 0;
	}

	@Override
	public int getFromMaximum(int denominator) {
		return denominator;
	}

	@Override
	public int getToMaximum(int denominator) {
		return denominator - 1;
	}

	@Override
	public boolean checkValueValidity(int from, int to, int denominator) {
		return ((from > 0) && ((denominator % from) == 0) && ((denominator / from) > to));
	}

	@Override
	public String generateFilter(int index, int numerator, int modulus, int denominator) {
		StringBuilder sb = new StringBuilder();

		int divisor = (denominator / numerator);
		for (int i = 0; i < denominator; i++) {
			if ((i % divisor) == modulus) {
				if (sb.length() > 0) {
					sb.append(" or ");
				}
				sb.append("(ip [4:2] & ");
				sb.append(denominator - 1);
				sb.append(" == ");
				sb.append(i);
				sb.append(")");
			}
		}

		return sb.toString();
	}

	@Override
	public String getFromText() {
		return "Numerator";
	}

	@Override
	public String getToText() {
		return "Modulus";
	}

}
