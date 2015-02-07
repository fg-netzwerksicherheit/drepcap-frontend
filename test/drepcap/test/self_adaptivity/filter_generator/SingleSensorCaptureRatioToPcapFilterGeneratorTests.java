/*
 *   Copyright 2014, Frankfurt University of Applied Sciences
 *
 *   This software is released under the terms of the Eclipse Public License 
 *   (EPL) 1.0. You can find a copy of the EPL at: 
 *   http://opensource.org/licenses/eclipse-1.0.php
 */

package drepcap.test.self_adaptivity.filter_generator;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import clojure.lang.Ratio;
import drepcap.frontend.self_adaptivity.filter_generator.SingleSensorCaptureRatioToPcapFilterGenerator;
import drepcap.frontend.util.CaptureRatioHelper;

/**
 * 
 * Tests for filter generation based on given capture ratio values.
 * 
 * @author Ruediger Gad
 *
 */
public class SingleSensorCaptureRatioToPcapFilterGeneratorTests {

	protected SingleSensorCaptureRatioToPcapFilterGenerator gen;

	@Before
	public void setup() {
		gen = new SingleSensorCaptureRatioToPcapFilterGenerator();
	}

	@Test
	public void testCaptureRatioOne() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(1, 64);
		Ratio offset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
		assertEquals("", gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}

	@Test
	public void testCaptureRatioHalf() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0.5, 64);
		Ratio offset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
		assertEquals("ip [4:2] & 63 < 31",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}

	@Test
	public void testCaptureRatioQuarter() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0.25, 64);
		Ratio offset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
		assertEquals("ip [4:2] & 63 < 15",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}

	@Test
	public void testCaptureRatioQuarterUnprecise() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0.249, 64);
		Ratio offset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
		assertEquals("ip [4:2] & 63 < 15",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}
	
	@Test
	public void testCaptureRatioZero() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0, 64);
		Ratio offset = new Ratio(BigInteger.ZERO, BigInteger.ONE);
		assertEquals("less 1",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}
	
	@Test
	public void testCaptureRatioQuarterWithOffset() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0.25, 64);
		Ratio offset = new Ratio(BigInteger.valueOf(10), BigInteger.valueOf(64));
		
		assertEquals("(ip [4:2] & 63 >= 10) and (ip [4:2] & 63 < 25)",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}
	
	@Test
	public void testCaptureRatioQuarterWithOffsetExceedingOne() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(0.25, 64);
		Ratio offset = new Ratio(BigInteger.valueOf(60), BigInteger.valueOf(64));
		
		assertEquals("ip [4:2] & 63 >= 60",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}
	
	@Test
	public void testCaptureRatioOneWithOffset() {
		Ratio cr = CaptureRatioHelper.calculateActualCaptureRatio(64, 64);
		Ratio offset = new Ratio(BigInteger.valueOf(10), BigInteger.valueOf(64));
		
		assertEquals("ip [4:2] & 63 >= 10",
				gen.generatePcapFilterFromCaptureRatio(cr, offset));
	}
}
