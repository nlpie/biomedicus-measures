/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import mockit.Tested;
import org.testng.annotations.Test;

public class DecimalNumberAcceptorTest {
  @Tested
  DecimalNumberAcceptor decimalNumberAcceptor;

  @Test
  public void testParseDecimalComma() throws Exception {
    decimalNumberAcceptor.tryToken("42,000", 0, 6);

    assertTrue(decimalNumberAcceptor.finish());
    assertEquals(decimalNumberAcceptor.getNumerator()
        .compareTo(BigDecimal.valueOf(42_000)), 0);
    assertEquals(decimalNumberAcceptor.getDenominator()
        .compareTo(BigDecimal.ONE), 0);
    assertEquals(decimalNumberAcceptor.getNumberType(), NumberType.DECIMAL);
    assertEquals(decimalNumberAcceptor.getBegin(), 0);
    assertEquals(decimalNumberAcceptor.getEnd(), 6);
  }

  @Test
  public void testParseDecimalCommaAndDecimal() throws Exception {
    decimalNumberAcceptor.tryToken("42,000,000.00", 0, 12);
    assertTrue(decimalNumberAcceptor.finish());
    assertEquals(decimalNumberAcceptor.getNumerator()
        .compareTo(BigDecimal.valueOf(42_000_000.00)), 0);
    assertEquals(decimalNumberAcceptor.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(decimalNumberAcceptor.getNumberType(), NumberType.DECIMAL);
    assertEquals(decimalNumberAcceptor.getBegin(), 0);
    assertEquals(decimalNumberAcceptor.getEnd(), 12);
  }

  @Test
  public void testParseDecimal() throws Exception {
    decimalNumberAcceptor.tryToken("450.01", 0, 6);
    assertTrue(decimalNumberAcceptor.finish());
    assertEquals(decimalNumberAcceptor.getNumerator().compareTo(BigDecimal.valueOf(450.01)),
        0);
    assertEquals(decimalNumberAcceptor.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(decimalNumberAcceptor.getNumberType(), NumberType.DECIMAL);
    assertEquals(decimalNumberAcceptor.getBegin(), 0);
    assertEquals(decimalNumberAcceptor.getEnd(), 6);
  }

  @Test
  public void testParseDecimalPercentage() throws Exception {
    decimalNumberAcceptor.tryToken("50.05", 0, 5);
    assertTrue(decimalNumberAcceptor.tryToken("%", 5, 6));
    assertEquals(decimalNumberAcceptor.getNumerator().compareTo(BigDecimal.valueOf(50.05)),
        0);
    assertEquals(decimalNumberAcceptor.getDenominator().compareTo(BigDecimal.valueOf(100)),
        0);
    assertEquals(decimalNumberAcceptor.getNumberType(), NumberType.FRACTION);
    assertEquals(decimalNumberAcceptor.getBegin(), 0);
    assertEquals(decimalNumberAcceptor.getEnd(), 6);
  }

  @Test
  public void testParseDecimalNoDecimal() throws Exception {
    assertFalse(decimalNumberAcceptor.tryToken("test", 0, 4));
    assertFalse(decimalNumberAcceptor.finish());
  }

  @Test
  public void testParseDecimalHyphen() throws Exception {
    assertFalse(decimalNumberAcceptor.tryToken("-", 0, 1));
    assertFalse(decimalNumberAcceptor.finish());
  }
}