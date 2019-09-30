/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.numbers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FractionNumberAcceptorTest {
  private FractionNumberDetector fractionNumberDetector = new FractionNumberDetector();

  @BeforeEach
  void setUp() {
    fractionNumberDetector.reset();
  }

  @Test
  void testEmptyToken() {
    assertTrue(fractionNumberDetector.tryToken("", 6, 6).isEmpty());
  }

  @Test
  void testEmptyTokenAfterNumber() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("", 2, 2);

    assertEquals(results.size(), 1);
    NumberResult numberResult = results.get(0);
    assertEquals(numberResult.getBegin(), 0);
    assertEquals(numberResult.getEnd(), 2);
    assertEquals(numberResult.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(numberResult.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(numberResult.getNumberType(), NumberType.DECIMAL);
  }

  @Test
  void testNumberNoFraction() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("people", 3, 9);

    assertEquals(results.size(), 1);
    NumberResult numberResult = results.get(0);
    assertEquals(numberResult.getBegin(), 0);
    assertEquals(numberResult.getEnd(), 2);
    assertEquals(numberResult.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(numberResult.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(numberResult.getNumberType(), NumberType.DECIMAL);
  }

  @Test
  void testNumberNoFractionFinish() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    List<NumberResult> results = fractionNumberDetector.finish();

    assertEquals(results.size(), 1);
    NumberResult numberResult = results.get(0);
    assertEquals(numberResult.getBegin(), 0);
    assertEquals(numberResult.getEnd(), 2);
    assertEquals(numberResult.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(numberResult.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(numberResult.getNumberType(), NumberType.DECIMAL);
  }

  @Test
  void testNumberThenFraction() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("25", 3,  5);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 2);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);

    assertTrue(fractionNumberDetector.tryToken("/", 5, 6).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("35", 6, 8).isEmpty());
    results = fractionNumberDetector.tryToken("units", 9, 14);

    assertEquals(results.size(), 1);
    result = results.get(0);
    assertEquals(result.getBegin(), 3);
    assertEquals(result.getEnd(), 8);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.valueOf(35)), 0);
    assertEquals(result.getNumberType(), NumberType.FRACTION);
  }

  @Test
  void testNumberSlashNumberSlash() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("/", 2, 3).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("35", 3, 5).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("/", 5, 6);
    assertEquals(results.size(), 2);
    NumberResult result = results.get(0);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 2);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);

    result = results.get(1);
    assertEquals(result.getBegin(), 3);
    assertEquals(result.getEnd(), 5);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(35)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
  }

  @Test
  void testFraction() {
    assertTrue(fractionNumberDetector.tryToken("25", 0, 2).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("/", 2, 3).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("35", 3, 5).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("units", 6, 11);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 5);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(25)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.valueOf(35)), 0);
    assertEquals(result.getNumberType(), NumberType.FRACTION);
  }

  @Test
  void testFractionDivideByZero() {
    assertTrue(fractionNumberDetector.tryToken("124", 0, 2).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("/", 2, 3).isEmpty());
    assertTrue(fractionNumberDetector.tryToken("0", 3, 4).isEmpty());
    List<NumberResult> results = fractionNumberDetector.tryToken("units", 5, 10);

    assertEquals(results.size(), 2);
    NumberResult result = results.get(0);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 2);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(124)), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
    result = results.get(1);
    assertEquals(result.getBegin(), 3);
    assertEquals(result.getEnd(), 4);
    assertEquals(result.getNumerator().compareTo(new BigDecimal(0)), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
  }
}
