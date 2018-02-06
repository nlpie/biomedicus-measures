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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;
import mockit.Tested;
import org.testng.annotations.Test;

public class DecimalNumberAcceptorTest {
  @Tested
  DecimalNumberAcceptor decimalNumberAcceptor;

  @Test
  public void testParseDecimalComma() throws Exception {
    NumberResult result = decimalNumberAcceptor.tryToken("42,000", 0, 6);

    assertNotNull(result);
    assertEquals(result.getNumerator().compareTo(BigDecimal.valueOf(42_000)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 6);
  }

  @Test
  public void testParseDecimalCommaAndDecimal() throws Exception {
    NumberResult result = decimalNumberAcceptor.tryToken("42,000,000.00", 0, 12);

    assertNotNull(result);
    assertEquals(result.getNumerator().compareTo(BigDecimal.valueOf(42_000_000.00)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 12);
  }

  @Test
  public void testParseDecimal() throws Exception {
    NumberResult result = decimalNumberAcceptor.tryToken("450.01", 0, 6);

    assertNotNull(result);
    assertEquals(result.getNumerator().compareTo(BigDecimal.valueOf(450.01)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.DECIMAL);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 6);
  }

  @Test
  public void testParseDecimalHyphen() throws Exception {
    assertNull(decimalNumberAcceptor.tryToken("-", 0, 1));
  }

  @Test
  public void testOrdinal() throws Exception {
    NumberResult result = decimalNumberAcceptor.tryToken("3rd", 0, 3);

    assertNotNull(result);
    assertEquals(result.getNumerator().compareTo(BigDecimal.valueOf(3)), 0);
    assertEquals(result.getDenominator().compareTo(BigDecimal.ONE), 0);
    assertEquals(result.getNumberType(), NumberType.ORDINAL);
    assertEquals(result.getBegin(), 0);
    assertEquals(result.getEnd(), 3);
  }
}