/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.testng.annotations.Test;

public class CombinedNumberDetectorTest {

  @Tested
  CombinedNumberDetector combinedNumberDetector;

  @Injectable
  EnglishNumeralsAcceptor englishNumeralsAcceptor;

  @Injectable
  DecimalNumberAcceptor decimalNumberAcceptor;

  @Test
  public void testEnglishTokenAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.tryToken("aTok", 0, 4); result = false;
      englishNumeralsAcceptor.tryToken("aTok", 0, 4); result = true;
      englishNumeralsAcceptor.getNumerator(); result = BigDecimal.ONE;
      englishNumeralsAcceptor.getDenominator(); result = BigDecimal.ONE;
      englishNumeralsAcceptor.getBegin(); result = 0;
      englishNumeralsAcceptor.getEnd(); result = 4;
      englishNumeralsAcceptor.getNumberType(); result = NumberType.ORDINAL;
    }};

    assertTrue(combinedNumberDetector.tryToken("aTok", 0, 4));
    assertEquals(combinedNumberDetector.getNumerator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getDenominator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getBegin(), 0);
    assertEquals(combinedNumberDetector.getEnd(), 4);
    assertEquals(combinedNumberDetector.getNumberType(), NumberType.ORDINAL);
  }

  @Test
  public void testEnglishFinishAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.finish(); result = false;
      englishNumeralsAcceptor.finish(); result = true;
      englishNumeralsAcceptor.getNumerator(); result = BigDecimal.ONE;
      englishNumeralsAcceptor.getDenominator(); result = BigDecimal.ONE;
      englishNumeralsAcceptor.getBegin(); result = 0;
      englishNumeralsAcceptor.getEnd(); result = 4;
      englishNumeralsAcceptor.getNumberType(); result = NumberType.ORDINAL;
    }};

    assertTrue(combinedNumberDetector.finish());
    assertEquals(combinedNumberDetector.getNumerator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getDenominator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getBegin(), 0);
    assertEquals(combinedNumberDetector.getEnd(), 4);
    assertEquals(combinedNumberDetector.getNumberType(), NumberType.ORDINAL);
  }

  @Test
  public void testDecimalTokenAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.tryToken("aTok", 0, 4); result = true;
      decimalNumberAcceptor.getNumerator(); result = BigDecimal.ONE;
      decimalNumberAcceptor.getDenominator(); result = BigDecimal.ONE;
      decimalNumberAcceptor.getBegin(); result = 0;
      decimalNumberAcceptor.getEnd(); result = 4;
      decimalNumberAcceptor.getNumberType(); result = NumberType.ORDINAL;
    }};

    assertTrue(combinedNumberDetector.tryToken("aTok", 0, 4));
    assertEquals(combinedNumberDetector.getNumerator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getDenominator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getBegin(), 0);
    assertEquals(combinedNumberDetector.getEnd(), 4);
    assertEquals(combinedNumberDetector.getNumberType(), NumberType.ORDINAL);
  }

  @Test
  public void testDecimalFinishAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.finish(); result = true;
      decimalNumberAcceptor.getNumerator(); result = BigDecimal.ONE;
      decimalNumberAcceptor.getDenominator(); result = BigDecimal.ONE;
      decimalNumberAcceptor.getBegin(); result = 0;
      decimalNumberAcceptor.getEnd(); result = 4;
      decimalNumberAcceptor.getNumberType(); result = NumberType.ORDINAL;
    }};

    assertTrue(combinedNumberDetector.finish());
    assertEquals(combinedNumberDetector.getNumerator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getDenominator(), BigDecimal.ONE);
    assertEquals(combinedNumberDetector.getBegin(), 0);
    assertEquals(combinedNumberDetector.getEnd(), 4);
    assertEquals(combinedNumberDetector.getNumberType(), NumberType.ORDINAL);
  }

  @Test
  public void testTokenNoAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.tryToken("aTok", 0, 4); result = false;
      englishNumeralsAcceptor.tryToken("aTok", 0, 4); result = false;
    }};

    assertFalse(combinedNumberDetector.tryToken("aTok", 0, 4));
  }

  @Test
  public void testFinishNoAccept() throws Exception {
    new Expectations() {{
      decimalNumberAcceptor.finish(); result = false;
      englishNumeralsAcceptor.finish(); result = false;
    }};

    assertFalse(combinedNumberDetector.finish());
  }
}