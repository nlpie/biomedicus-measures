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

import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.BasicNumberAcceptor;
import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.NonFractionAcceptor;
import java.math.BigDecimal;
import java.util.List;
import mockit.Expectations;
import mockit.Injectable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnglishNumeralsAcceptorTest {

  private static NumberDefinition oneDef = new NumberDefinition(1, BasicNumberType.UNIT);

  private static NumberDefinition fiveDef = new NumberDefinition(5, BasicNumberType.UNIT);

  private static NumberDefinition fortyDef = new NumberDefinition(40,
      BasicNumberType.DECADE);

  private static NumberDefinition fourDef = new NumberDefinition(4,  BasicNumberType.UNIT);

  private static NumberDefinition tenDef = new NumberDefinition(10, BasicNumberType.TEEN);

  private static NumberDefinition fifteenDef = new NumberDefinition(15,
      BasicNumberType.TEEN);

  private static NumberDefinition hundredDef = new NumberDefinition(100, BasicNumberType.MAGNITUDE);

  private static NumberDefinition billionDef = new NumberDefinition(3, BasicNumberType.MAGNITUDE);

  private static NumberDefinition millionDef = new NumberDefinition(2, BasicNumberType.MAGNITUDE);

  private static NumberDefinition sixths = new NumberDefinition(6, BasicNumberType.UNIT);

  private static NumberDefinition halfDef = new NumberDefinition(2, BasicNumberType.UNIT);

  @Injectable
  private NumberModel numbers;

  private EnglishNumeralsAcceptor fractionAcceptor;

  private NonFractionAcceptor numberAcceptor;

  private BasicNumberAcceptor basicNumberAcceptor;

  @BeforeEach
  void setUp() {
    basicNumberAcceptor = new BasicNumberAcceptor(numbers);
    numberAcceptor = new EnglishNumeralsAcceptor.NonFractionAcceptor(numbers, basicNumberAcceptor);
    fractionAcceptor = new EnglishNumeralsAcceptor(numberAcceptor);
  }

  @Test
  void testBasicRecognizesUnit() {
    new Expectations() {{
      numbers.getNumberDefinition("four"); result = fourDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken("four", 0, 4));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 4);
    assertEquals(basicNumberAcceptor.value, 4);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesTeen() {
    new Expectations() {{
      numbers.getNumberDefinition("ten"); result = tenDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken("ten", 0, 3));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 3);
    assertEquals(basicNumberAcceptor.value, 10);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesDecade() {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("people"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("people", 7, 13));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 6);
    assertEquals(basicNumberAcceptor.value, 40);
    assertFalse(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecongizesDecadeAnd() {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("five", 7, 12));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicRecognizesDecadeHyphen() {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("-"); result = null;
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertFalse(basicNumberAcceptor.tryToken("-", 6, 7));
    assertTrue(basicNumberAcceptor.tryToken("five", 7, 12));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
    assertTrue(basicNumberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicDecadeHyphenUnrelated() {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("-"); result = null; times = 2;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("the"); result = null;
      numbers.getOrdinal("the"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken("the", 0, 3));
  }

  @Test
  void testMagnitude() {
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getNumberDefinition("billion"); result = billionDef;
      numbers.getOrdinal("people"); result = null;
      numbers.getNumberDefinition("people"); result = null;
    }};

    assertFalse(numberAcceptor.tryToken("five", 0, 4));
    assertFalse(numberAcceptor.tryToken("billion", 5, 12));
    assertTrue(numberAcceptor.tryToken("people", 13, 19));

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(10).pow(9)));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
    assertFalse(numberAcceptor.consumedLastToken);
  }

  @Test
  void testBasicOnly() {
    new Expectations() {{
      numbers.getNumberDefinition("four"); result = fourDef;
    }};

    assertFalse(numberAcceptor.tryToken("four", 0, 4));
    assertTrue(numberAcceptor.tryToken("people", 5, 11));

    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 4);
    assertEquals(numberAcceptor.value, BigDecimal.valueOf(4));
    assertFalse(numberAcceptor.consumedLastToken);
  }

  @Test
  void testHundred() {
    new Expectations() {{
      numbers.getNumberDefinition("fifteen"); result = fifteenDef;
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getNumberDefinition("billion"); result = billionDef;
      numbers.getNumberDefinition("million"); result = millionDef;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getNumberDefinition("hundred"); result = hundredDef;
    }};

    assertFalse(numberAcceptor.tryToken("five", 0, 4));
    assertFalse(numberAcceptor.tryToken("hundred", 5, 12));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(500));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
  }

  @Test
  void testEndOfSentenceDecade() {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
    }};

    assertFalse(numberAcceptor.tryToken("forty", 0, 5));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigDecimal.valueOf(40));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 5);
  }

  @Test
  void testFraction() {
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef; minTimes = 1;
      numbers.getDenominator("forty"); result = null; minTimes = 1;
      numbers.getNumberDefinition("forty"); result = fortyDef; minTimes = 1;
      numbers.getDenominator("sixths"); result = sixths; minTimes = 1;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("sixths"); result = null;
      numbers.getDenominator("sixths"); result = sixths;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getNumberDefinition("hundred"); result = hundredDef;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getNumberDefinition("5"); result = null;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("one"); result = oneDef;
      numbers.getNumberDefinition("half"); result = null;
      numbers.getDenominator("half"); result = halfDef;
    }};

    assertTrue(fractionAcceptor.tryToken("one", 0, 3).isEmpty());
    List<NumberResult> results = fractionAcceptor.tryToken("half", 4, 8);
    assertEquals(results.size(), 1);
    NumberResult result = results.get(0);
    assertEquals(result.getNumerator(), BigDecimal.ONE);
    assertEquals(result.getDenominator(), new BigDecimal(2));
  }

  @Test
  void testOneHyphenHalf() {
    new Expectations() {{
      numbers.getNumberDefinition("one"); result = oneDef;
      numbers.getNumberDefinition("half"); result = null;
      numbers.getDenominator("half"); result = halfDef;

    }};

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
    new Expectations() {{
      numbers.getNumberDefinition("four"); result = fourDef; minTimes = 1;
      numbers.getNumberDefinition("hours"); result = null; minTimes = 1;
      numbers.getDenominator("hours"); result = null; minTimes = 1;
    }};

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
    new Expectations() {{
      numbers.getNumberDefinition(""); result = null; minTimes = 1;
    }};

    assertTrue(fractionAcceptor.tryToken("", 6, 6).isEmpty());
  }
}