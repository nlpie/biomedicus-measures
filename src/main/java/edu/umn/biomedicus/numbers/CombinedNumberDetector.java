/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

import java.math.BigDecimal;

/**
 * Detects both decimal numbers using {@link DecimalNumberAcceptor} and english numbers using {@link
 * EnglishNumeralsAcceptor}.
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

  CombinedNumberDetector(DecimalNumberAcceptor decimalAcceptor,
      EnglishNumeralsAcceptor englishAcceptor) {
    this.decimalAcceptor = decimalAcceptor;
    this.englishAcceptor = englishAcceptor;
    reset();
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
   * Resets this number detector, clearing the information about the last detected number and
   * getting it ready to try to detect another number.
   */
  public void reset() {
    decimalAcceptor.reset();
    englishAcceptor.reset();
    begin = -1;
    end = -1;
    numerator = null;
    denominator = null;
    numberType = null;
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
  }

  private void copyFromDecimal() {
    begin = decimalAcceptor.getBegin();
    end = decimalAcceptor.getEnd();
    numberType = decimalAcceptor.getNumberType();
    numerator = decimalAcceptor.getNumerator();
    denominator = decimalAcceptor.getDenominator();
  }
}
