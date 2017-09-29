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

import static org.testng.Assert.*;

import java.math.BigDecimal;
import mockit.Injectable;
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
    assertTrue(decimalNumberAcceptor.getConsumedLastToken());
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

  @Test
  public void testNoIncludePercent() throws Exception {
    DecimalNumberAcceptor decimalNumberAcceptor = new DecimalNumberAcceptor(false, false);
    decimalNumberAcceptor.tryToken("50.05", 0, 5);
    assertTrue(decimalNumberAcceptor.tryToken("%", 5, 6));
    assertEquals(decimalNumberAcceptor.getNumerator().compareTo(BigDecimal.valueOf(50.05)),
        0);
    assertEquals(decimalNumberAcceptor.getDenominator().compareTo(BigDecimal.valueOf(1)),
        0);
    assertEquals(decimalNumberAcceptor.getNumberType(), NumberType.DECIMAL);
    assertEquals(decimalNumberAcceptor.getBegin(), 0);
    assertEquals(decimalNumberAcceptor.getEnd(), 5);
    assertFalse(decimalNumberAcceptor.getConsumedLastToken());
  }

  @Test
  public void testConsumedLastToken() throws Exception {
    assertFalse(decimalNumberAcceptor.tryToken("25", 0, 2));
    assertTrue(decimalNumberAcceptor.tryToken("5", 3, 4));
    assertFalse(decimalNumberAcceptor.getConsumedLastToken());
    decimalNumberAcceptor.reset();
    assertFalse(decimalNumberAcceptor.tryToken("5", 3, 4));
    assertTrue(decimalNumberAcceptor.finish());
  }
}