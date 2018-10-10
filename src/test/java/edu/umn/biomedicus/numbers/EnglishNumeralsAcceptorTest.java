/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.BasicNumberAcceptor;
import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.NonFractionAcceptor;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnglishNumeralsAcceptorTest {

  private static NumberDefinition oneDef = new NumberDefinition(1, BasicNumberType.UNIT);

  private static NumberDefinition fiveDef = new NumberDefinition(5, BasicNumberType.UNIT);

  private static NumberDefinition fortyDef = new NumberDefinition(40,
      BasicNumberType.DECADE);

  private static NumberDefinition fourDef = new NumberDefinition(4, BasicNumberType.UNIT);

  private static NumberDefinition tenDef = new NumberDefinition(10, BasicNumberType.TEEN);

  private static NumberDefinition fifteenDef = new NumberDefinition(15,
      BasicNumberType.TEEN);

  private static NumberDefinition hundredDef = new NumberDefinition(100, BasicNumberType.MAGNITUDE);

  private static NumberDefinition billionDef = new NumberDefinition(3, BasicNumberType.MAGNITUDE);

  private static NumberDefinition millionDef = new NumberDefinition(2, BasicNumberType.MAGNITUDE);

  private static NumberDefinition sixths = new NumberDefinition(6, BasicNumberType.UNIT);

  private static NumberDefinition halfDef = new NumberDefinition(2, BasicNumberType.UNIT);

  private NumberModel numbers;

  private EnglishNumeralsAcceptor fractionAcceptor;

  private NonFractionAcceptor numberAcceptor;

  private BasicNumberAcceptor basicNumberAcceptor;

  @BeforeEach
  void setUp() {
    numbers = mock(NumberModel.class);
    basicNumberAcceptor = new BasicNumberAcceptor(numbers);
    numberAcceptor = new EnglishNumeralsAcceptor.NonFractionAcceptor(numbers, basicNumberAcceptor);
    fractionAcceptor = new EnglishNumeralsAcceptor(numberAcceptor);
  }

  @Test
  void testBasicRecognizesUnit() {
    when(numbers.getNumberDefinition("four")).thenReturn(fourDef);

    assertTrue(basicNumberAcceptor.tryToken("four", 0, 4));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 4);
    assertEquals(basicNumberAcceptor.value, 4);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesTeen() {
    when(numbers.getNumberDefinition("ten")).thenReturn(tenDef);

    assertTrue(basicNumberAcceptor.tryToken("ten", 0, 3));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 3);
    assertEquals(basicNumberAcceptor.value, 10);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesDecade() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("people")).thenReturn(null);

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("people", 7, 13));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 6);
    assertEquals(basicNumberAcceptor.value, 40);
    assertFalse(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecongizesDecadeAnd() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("five", 7, 12));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesDecadeHyphen() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("-")).thenReturn(null);
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertFalse(basicNumberAcceptor.tryToken("-", 6, 7));
    assertTrue(basicNumberAcceptor.tryToken("five", 7, 12));

    assertEquals(0, basicNumberAcceptor.begin);
    assertEquals(12, basicNumberAcceptor.end);
    assertEquals(45, basicNumberAcceptor.value);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicDecadeHyphenUnrelated() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("-")).thenReturn(null);

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 5));
    assertFalse(basicNumberAcceptor.tryToken("-", 5, 6));
    assertTrue(basicNumberAcceptor.tryToken("-", 6, 7));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 5);
    assertEquals(basicNumberAcceptor.value, 40);
    assertFalse(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRandomWord() {
    when(numbers.getNumberDefinition("the")).thenReturn(null);
    when(numbers.getOrdinal("the")).thenReturn(null);

    assertFalse(basicNumberAcceptor.tryToken("the", 0, 3));
  }

  @Test
  void testMagnitude() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getNumberDefinition("billion")).thenReturn(billionDef);
    when(numbers.getOrdinal("people")).thenReturn(null);
    when(numbers.getNumberDefinition("people")).thenReturn(null);

    assertFalse(numberAcceptor.tryToken("five", 0, 4));
    assertFalse(numberAcceptor.tryToken("billion", 5, 12));
    assertTrue(numberAcceptor.tryToken("people", 13, 19));

    assertEquals(numberAcceptor.value,
        BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(10).pow(9)));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
    assertFalse(numberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicOnly() {
    when(numbers.getNumberDefinition("four")).thenReturn(fourDef);

    assertFalse(numberAcceptor.tryToken("four", 0, 4));
    assertTrue(numberAcceptor.tryToken("people", 5, 11));

    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 4);
    assertEquals(numberAcceptor.value, BigDecimal.valueOf(4));
    assertFalse(numberAcceptor.consumedLastToken);
  }

  @Test
  void testHundred() {
    when(numbers.getNumberDefinition("fifteen")).thenReturn(fifteenDef);
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);

    assertFalse(numberAcceptor.tryToken("fifteen", 0, 7));
    assertFalse(numberAcceptor.tryToken("hundred", 8, 15));
    assertFalse(numberAcceptor.tryToken("forty", 16, 21));
    assertFalse(numberAcceptor.tryToken("five", 22, 26));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(1545));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 26);
  }

  @Test
  void testChained() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getNumberDefinition("billion")).thenReturn(billionDef);
    when(numbers.getNumberDefinition("million")).thenReturn(millionDef);

    assertFalse(numberAcceptor.tryToken("five", 0, 4));
    assertFalse(numberAcceptor.tryToken("billion", 5, 12));
    assertFalse(numberAcceptor.tryToken("five", 13, 17));
    assertFalse(numberAcceptor.tryToken("million", 18, 25));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(10).pow(9))
        .add(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(10).pow(6))));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 25);
  }

  @Test
  void testEndOfSentenceHundred() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getNumberDefinition("hundred")).thenReturn(hundredDef);

    assertFalse(numberAcceptor.tryToken("five", 0, 4));
    assertFalse(numberAcceptor.tryToken("hundred", 5, 12));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(500));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
  }

  @Test
  void testEndOfSentenceDecade() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);

    assertFalse(numberAcceptor.tryToken("forty", 0, 5));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(40));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 5);
  }

  @Test
  void testFraction() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getDenominator("forty")).thenReturn(null);
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getDenominator("sixths")).thenReturn(sixths);

    assertTrue(fractionAcceptor.tryToken("five", 0, 4).isEmpty());
    assertTrue(fractionAcceptor.tryToken("forty", 5, 10).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("sixths", 11, 17);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(5));
    assertEquals(result.getDenominator(), BigDecimal.valueOf(46));
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 17);
  }

  @Test
  void testFractionTwoWordFraction() {
    when(numbers.getNumberDefinition("forty")).thenReturn(fortyDef);
    when(numbers.getNumberDefinition("sixths")).thenReturn(null);
    when(numbers.getDenominator("sixths")).thenReturn(sixths);

    assertTrue(fractionAcceptor.tryToken("forty", 0, 5).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("sixths", 6, 12);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(40));
    assertEquals(result.getDenominator(), BigDecimal.valueOf(6));
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 12);
  }

  @Test
  void testAndHalf() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);

    assertTrue(fractionAcceptor.tryToken("five", 0, 4).isEmpty());
    assertTrue(fractionAcceptor.tryToken("and", 5, 8).isEmpty());
    assertTrue(fractionAcceptor.tryToken("a", 9, 10).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("half", 11, 15);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(11));
    assertEquals(result.getDenominator(), BigDecimal.valueOf(2));
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 15);
    assertEquals(result.getNumberType(), NumberType.FRACTION);
  }

  @Test
  void testFractionAcceptorNumeratorFinish() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getNumberDefinition("hundred")).thenReturn(hundredDef);

    assertTrue(fractionAcceptor.tryToken("five", 0, 4).isEmpty());
    assertTrue(fractionAcceptor.tryToken("hundred", 5, 12).isEmpty());

    List<NumberResult> results = fractionAcceptor.finish();
    assertEquals(results.size(), 1);

    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(500));
    assertEquals(result.getNumberType(), NumberType.CARDINAL);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 12);
  }

  @Test
  void testConsumedLastToken() {
    when(numbers.getNumberDefinition("five")).thenReturn(fiveDef);
    when(numbers.getNumberDefinition("5")).thenReturn(null);

    assertTrue(fractionAcceptor.tryToken("five", 0, 4).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("5", 5, 6);

    assertEquals(results.size(), 1);

    NumberResult result = results.get(0);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 4);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(5));
    assertEquals(result.getDenominator(), BigDecimal.ONE);
    assertEquals(result.getNumberType(), NumberType.CARDINAL);
  }

  @Test
  void testOneHalf() {
    when(numbers.getNumberDefinition("one")).thenReturn(oneDef);
    when(numbers.getNumberDefinition("half")).thenReturn(null);
    when(numbers.getDenominator("half")).thenReturn(halfDef);

    assertTrue(fractionAcceptor.tryToken("one", 0, 3).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("half", 4, 8);
    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.ONE);
    assertEquals(result.getDenominator(), new BigDecimal(2));
  }

  @Test
  void testOneHyphenHalf() {
    when(numbers.getNumberDefinition("one")).thenReturn(oneDef);
    when(numbers.getNumberDefinition("half")).thenReturn(null);
    when(numbers.getDenominator("half")).thenReturn(halfDef);

    assertTrue(fractionAcceptor.tryToken("one", 0, 3).isEmpty());
    assertTrue(fractionAcceptor.tryToken("-", 3, 4).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("half", 4, 8);
    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.ONE);
    assertEquals(result.getDenominator(), new BigDecimal(2));
  }

  @Test
  void testNumberUnrelated() {
    when(numbers.getNumberDefinition("four")).thenReturn(fourDef);
    when(numbers.getNumberDefinition("hours")).thenReturn(null);
    when(numbers.getDenominator("hours")).thenReturn(null);

    assertTrue(fractionAcceptor.tryToken("four", 0, 4).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("hours", 5, 9);

    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.valueOf(4));
    assertEquals(result.getDenominator(), BigDecimal.valueOf(1));
    assertEquals(result.getNumberType(), NumberType.CARDINAL);
  }

  @Test
  void testEmptyToken() {
    when(numbers.getNumberDefinition("")).thenReturn(null);

    assertTrue(fractionAcceptor.tryToken("", 6, 6).isEmpty());
  }
}
