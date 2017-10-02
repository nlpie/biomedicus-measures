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

import java.math.BigDecimal;

/**
 * Detects both decimal numbers using {@link DecimalNumberAcceptor} and english numbers using {@link
 * EnglishNumeralsAcceptor}.
 *
 * <pre>
    {@code
Iterator<String> iterator = tokens.iterator();
String token = null;
while (true) {
  if (token == null) {
    if (!iterator.hasNext()) {
      break;
    }
    token = iterator.next();
  }

  int begin = tokenLabel.getBegin();
  int end = tokenLabel.getEnd();

  if (numberDetector.tryToken(text, begin, end)) {
    // do something with detected number
    if (!numberDetector.getConsumedLastToken()) {
      continue;
    }
  }

  token = null;
}

if (numberDetector.finish()) {
  // do something with detected number
}
    }
 * </pre>
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 1.0.0
 */
public class CombinedNumberDetector {

  private final DecimalNumberAcceptor decimalAcceptor;

  private final EnglishNumeralsAcceptor englishAcceptor;

  private int begin;

  private int end;

  private BigDecimal numerator;

  private BigDecimal denominator;

  private NumberType numberType;

  private boolean consumedLastToken;

  CombinedNumberDetector(DecimalNumberAcceptor decimalAcceptor,
      EnglishNumeralsAcceptor englishAcceptor) {
    this.decimalAcceptor = decimalAcceptor;
    this.englishAcceptor = englishAcceptor;
  }

  /**
   * Returns the beginning of the detected number. Not valid until true has been returned by {@link
   * #tryToken(String, int, int)} or {@link #finish()}
   *
   * @return integer begin index
   */
  public int getBegin() {
    return begin;
  }

  /**
   * Returns the end of the detected number. Not valid until true has been returned by {@link
   * #tryToken(String, int, int)} or {@link #finish()}
   *
   * @return integer end index
   */
  public int getEnd() {
    return end;
  }

  /**
   * Returns the numerator of a detected number. Not valid until true has been returned by {@link
   * #tryToken(String, int, int)} or {@link #finish()}
   *
   * @return a {@link BigDecimal} representation of the numerator
   */
  public BigDecimal getNumerator() {
    return numerator;
  }

  /**
   * Returns the denominator of a detected number. Not valid until true has been returned by {@link
   * #tryToken(String, int, int)} or {@link #finish()}.
   *
   * @return a {@link BigDecimal} representation of the denominator.
   */
  public BigDecimal getDenominator() {
    return denominator;
  }

  /**
   * Returns the {@link NumberType} of a detected number. Not valid until true has been returned by
   * {@link #tryToken(String, int, int)} or {@link #finish()}.
   *
   * @return a {@link BigDecimal} representation of the number type.
   */
  public NumberType getNumberType() {
    return numberType;
  }

  /**
   * After {@link #tryToken(String, int, int)} returns true, this stores whether or not the last
   * token passed to this detector was consumed in creating the number. If it was not, it needs to
   * be passed again to detect whether or not it is part of a number.
   *
   * @return true if the last token passed was consumed in creating the number.
   */
  public boolean getConsumedLastToken() {
    return consumedLastToken;
  }

  /**
   * Passes the number detector the specified token, seeing if it has detected a number from the
   * tokens that this instance has received.
   *
   * @param token the token to check
   * @param tokenBegin an identifying begin index for the token
   * @param tokenEnd an identifying end index for the token
   * @return {@code true} if this token means that this class has seen a number in the tokens that
   * have been passed to it, {@code false} otherwise.
   */
  public boolean tryToken(String token, int tokenBegin, int tokenEnd) {
    if (decimalAcceptor.tryToken(token, tokenBegin, tokenEnd)) {
      copyFromDecimal();
      return true;
    } else if (englishAcceptor.tryToken(token, tokenBegin, tokenEnd)) {
      copyFromEnglish();
      return true;
    }

    return false;
  }

  /**
   * Informs this instance that it is done being passed tokens, and to check if any of the tokens it
   * received previously create a number.
   *
   * @return {@code true} if the tokens it has received contain a number, {@code false} otherwise
   */
  public boolean finish() {
    if (decimalAcceptor.finish()) {
      copyFromDecimal();
      return true;
    } else if (englishAcceptor.finish()) {
      copyFromEnglish();
      return true;
    }
    return false;
  }

  private void copyFromEnglish() {
    begin = englishAcceptor.getBegin();
    end = englishAcceptor.getEnd();
    numberType = englishAcceptor.getNumberType();
    numerator = englishAcceptor.getNumerator();
    denominator = englishAcceptor.getDenominator();
    consumedLastToken = englishAcceptor.getConsumedLastToken();
    englishAcceptor.reset();
    decimalAcceptor.reset();
  }

  private void copyFromDecimal() {
    begin = decimalAcceptor.getBegin();
    end = decimalAcceptor.getEnd();
    numberType = decimalAcceptor.getNumberType();
    numerator = decimalAcceptor.getNumerator();
    denominator = decimalAcceptor.getDenominator();
    consumedLastToken = decimalAcceptor.getConsumedLastToken();
    englishAcceptor.reset();
    decimalAcceptor.reset();
  }
}
