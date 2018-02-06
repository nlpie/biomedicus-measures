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

import java.math.BigDecimal;

/**
 * A result of number detection.
 *
 * @since 2.0.0
 */
public class NumberResult {
  private final int begin;

  private final int end;

  private final BigDecimal numerator;

  private final BigDecimal denominator;

  private final NumberType numberType;

  public NumberResult(
      int begin,
      int end,
      BigDecimal numerator,
      BigDecimal denominator,
      NumberType numberType
  ) {
    this.begin = begin;
    this.end = end;
    this.numerator = numerator;
    this.denominator = denominator;
    this.numberType = numberType;
  }

  /**
   * The begin of the detected token.
   *
   * @return the integer offset before the first character in the number
   */
  public int getBegin() {
    return begin;
  }

  /**
   * The end of the detected token.
   *
   * @return the integer offset after the last character in the number
   */
  public int getEnd() {
    return end;
  }

  /**
   * The numerator value of the number.
   *
   * @return a BigDecimal containing the numerator value
   */
  public BigDecimal getNumerator() {
    return numerator;
  }

  /**
   * The denominator value of the number.
   *
   * @return a BigDecimal containing the denominator value
   */
  public BigDecimal getDenominator() {
    return denominator;
  }

  /**
   * The type of the number
   *
   * @return the number type, e.g. NumberType.CARDINAL, NumberType.ORDINAL, etc
   */
  public NumberType getNumberType() {
    return numberType;
  }
}
