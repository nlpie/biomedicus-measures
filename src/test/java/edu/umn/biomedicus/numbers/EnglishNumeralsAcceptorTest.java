/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

import static org.testng.Assert.*;

import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.BasicNumberAcceptor;
import edu.umn.biomedicus.numbers.EnglishNumeralsAcceptor.NonFractionAcceptor;
import java.math.BigDecimal;
import mockit.Expectations;
import mockit.Injectable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EnglishNumeralsAcceptorTest {
  static NumberDefinition fiveDef = new NumberDefinition(5, BasicNumberType.UNIT);

  static NumberDefinition fortyDef = new NumberDefinition(40,
      BasicNumberType.DECADE);

  static NumberDefinition fourDef = new NumberDefinition(4,  BasicNumberType.UNIT);

  static NumberDefinition tenDef = new NumberDefinition(10, BasicNumberType.TEEN);

  static NumberDefinition fifteenDef = new NumberDefinition(15,
      BasicNumberType.TEEN);

  static NumberDefinition hundredDef = new NumberDefinition(100, BasicNumberType.MAGNITUDE);

  static NumberDefinition billionDef = new NumberDefinition(3, BasicNumberType.MAGNITUDE);

  static NumberDefinition millionDef = new NumberDefinition(2, BasicNumberType.MAGNITUDE);

  static NumberDefinition sixths = new NumberDefinition(6, BasicNumberType.UNIT);

  @Injectable
  NumberModel numbers;

  EnglishNumeralsAcceptor fractionAcceptor;

  NonFractionAcceptor numberAcceptor;

  BasicNumberAcceptor basicNumberAcceptor;

  @BeforeMethod
  public void setUp() {
    basicNumberAcceptor = new BasicNumberAcceptor(numbers);
    numberAcceptor = new EnglishNumeralsAcceptor.NonFractionAcceptor(numbers, basicNumberAcceptor);
    fractionAcceptor = new EnglishNumeralsAcceptor(numberAcceptor);
  }

  @Test
  public void testBasicRecognizesUnit() {
    new Expectations() {{
      numbers.getNumberDefinition("four"); result = fourDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken("four", 0, 4));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 4);
    assertEquals(basicNumberAcceptor.value, 4);
  }

  @Test
  public void testBasicRecognizesTeen() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("ten"); result = tenDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken("ten", 0, 3));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 3);
    assertEquals(basicNumberAcceptor.value, 10);
  }

  @Test
  public void testBasicRecognizesDecade() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("people"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("people", 7, 13));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 6);
    assertEquals(basicNumberAcceptor.value, 40);
  }

  @Test
  public void testBasicRecongizesDecadeAnd() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(basicNumberAcceptor.tryToken("forty", 0, 6));
    assertTrue(basicNumberAcceptor.tryToken("five", 7, 12));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
  }

  @Test
  public void testBasicRecognizesDecadeHyphen() {
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
  }

  @Test
  public void testBasicRandomWord() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("the"); result = null;
      numbers.getOrdinal("the"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken("the", 0, 3));
  }

  @Test
  public void testMagnitude() throws Exception {
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
  }

  @Test
  public void testBasicOnly() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("four"); result = fourDef;
    }};

    assertFalse(numberAcceptor.tryToken("four", 0, 4));
    assertTrue(numberAcceptor.tryToken("people", 5, 11));

    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 4);
    assertEquals(numberAcceptor.value, BigDecimal.valueOf(4));
  }

  @Test
  public void testHundred() throws Exception {
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
  public void testChained() throws Exception {
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
  public void testEndOfSentenceHundred() throws Exception {
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
  public void testEndOfSentenceDecade() throws Exception {
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
  public void testFraction() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
      numbers.getDenominator("forty"); result = null;
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getDenominator("sixths"); result = sixths;
    }};

    assertFalse(fractionAcceptor.tryToken("five", 0, 4));
    assertFalse(fractionAcceptor.tryToken("forty", 5, 10));
    assertTrue(fractionAcceptor.tryToken("sixths", 11, 17));

    assertEquals(fractionAcceptor.getNumerator(), BigDecimal.valueOf(5));
    assertEquals(fractionAcceptor.getDenominator(), BigDecimal.valueOf(46));
    assertEquals(fractionAcceptor.getBegin(), 0);
    assertEquals(fractionAcceptor.getEnd(), 17);
  }

  @Test
  public void testFractionTwoWordFraction() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("forty"); result = fortyDef;
      numbers.getNumberDefinition("sixths"); result = null;
      numbers.getDenominator("sixths"); result = sixths;
    }};

    assertFalse(fractionAcceptor.tryToken("forty", 0, 5));
    assertTrue(fractionAcceptor.tryToken("sixths", 6, 12));

    assertEquals(fractionAcceptor.getNumerator(), BigDecimal.valueOf(40));
    assertEquals(fractionAcceptor.getDenominator(), BigDecimal.valueOf(6));
    assertEquals(fractionAcceptor.getBegin(), 0);
    assertEquals(fractionAcceptor.getEnd(), 12);
  }

  @Test
  public void testAndHalf() throws Exception {
    new Expectations() {{
      numbers.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(fractionAcceptor.tryToken("five", 0, 4));
    assertFalse(fractionAcceptor.tryToken("and", 5, 8));
    assertFalse(fractionAcceptor.tryToken("a", 9, 10));
    assertTrue(fractionAcceptor.tryToken("half", 11, 15));

    assertEquals(fractionAcceptor.getNumerator(), BigDecimal.valueOf(11));
    assertEquals(fractionAcceptor.getDenominator(), BigDecimal.valueOf(2));
    assertEquals(fractionAcceptor.getBegin(), 0);
    assertEquals(fractionAcceptor.getEnd(), 15);
    assertEquals(fractionAcceptor.getNumberType(), NumberType.FRACTION);
  }
}