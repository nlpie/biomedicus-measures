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
 * Finds decimal numbers in text, including fractions like 1 / 2 separated over separate tokens.
 * Also detects hybrid ordinals, like "1st" "2nd" "3rd", etc.
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 1.0.0
 */
public class DecimalNumberAcceptor {

  private int begin;

  private int end;

  private boolean isOrdinal;

  private BigDecimal numerator;

  private BigDecimal denominator;

  private NumberType numberType;

  private boolean fraction;

  /**
   * Default constructor.
   */
  public DecimalNumberAcceptor() {
    reset();
  }

  /**
   * Returns the begin of the detected number. Not valid until
   * {@link #tryToken(CharSequence, int, int)} or {@link #finish()} have returned true.
   *
   * @return integer begin index of the detected number.
   */
  public int getBegin() {
    return begin;
  }

  /**
   * Returns the end of the detected number.
   *
   * @return integer end index of the detected number.
   */
  public int getEnd() {
    return end;
  }

  /**
   * Returns whether or not the detected number is an ordinal.
   */
  public boolean isOrdinal() {
    return isOrdinal;
  }

  /**
   *
   * @return
   */
  public BigDecimal getNumerator() {
    return numerator;
  }

  /**
   *
   * @return
   */
  public BigDecimal getDenominator() {
    return denominator;
  }

  public NumberType getNumberType() {
    return numberType;
  }

  public void reset() {
    begin = -1;
    end = -1;
    numerator = null;
    denominator = null;
    isOrdinal = false;
    numberType = null;
    fraction = false;
  }

  /**
   * Parses any decimal numbers from the token text. After it has detected a decimal number it will
   * return true. The number may not necessarily, and will not in many cases contain the token that
   * was just passed.
   *
   * @param token text to parse
   */
  public boolean tryToken(CharSequence token, int tokenBegin, int tokenEnd) {
    if (numerator != null) {
      if (!fraction) {
        if (token.equals("/")) {
          fraction = true;
          return false;
        } else if (token.equals("%")) {
          denominator = BigDecimal.valueOf(100);
          numberType = NumberType.FRACTION;
          end = tokenEnd;
          return true;
        } else {
          numberType = NumberType.DECIMAL;
          denominator = BigDecimal.ONE;
          return true;
        }
      }
    }

    isOrdinal = false;
    char ch = token.charAt(0);

    StringBuilder digits;
    boolean negative = false;
    if (ch == '+') {
      digits = new StringBuilder();
    } else if (ch == '-') {
      digits = new StringBuilder();
      negative = true;
    } else if (Character.isDigit(ch)) {
      digits = new StringBuilder();
      digits.append(ch);
    } else {
      if (numerator != null) {
        denominator = BigDecimal.ONE;
        numberType = NumberType.DECIMAL;
        return true;
      }
      return false;
    }

    int period = -1;
    boolean percentage = false;
    for (int i = 1; i < token.length(); i++) {
      ch = token.charAt(i);
      if (ch == ',') {
        continue;
      }

      if (ch == '.') {
        period = digits.length();
      } else if (Character.isDigit(ch)) {
        digits.append(ch);
      } else if (i == token.length() - 1 && ch == '%') {
        percentage = true;
      } else {
        if (i + 1 < token.length()) {
          if ((ch == 't' && token.charAt(i + 1) == 'h')
              || (ch == 's' && token.charAt(i + 1) == 't')
              || (ch == 'n' && token.charAt(i + 1) == 'd')
              || (ch == 'r' && token.charAt(i + 1) == 'd')) {
            isOrdinal = true;
            break;
          }
        }
        if (numerator != null) {
          denominator = BigDecimal.ONE;
          numberType = NumberType.DECIMAL;
          return true;
        }

        return false;
      }
    }

    if (digits.length() == 0) {
      return numerator != null;
    }

    BigDecimal value = BigDecimal.ZERO;
    BigDecimal ten = BigDecimal.valueOf(10);

    if (period != -1) {
      for (int i = 0; i < period; i++) {
        value = value.multiply(ten).add(new BigDecimal("" + digits.charAt(i)));
      }
      for (int i = period; i < digits.length(); i++) {
        value = value.add(new BigDecimal("" + digits.charAt(i))
            .divide(ten.pow(i - period + 1)));
      }
    } else {
      for (int i = 0; i < digits.length(); i++) {
        value = value.multiply(ten).add(new BigDecimal("" + digits.charAt(i)));
      }
    }
    if (negative) {
      value = value.negate();
    }
    if (percentage) {
      value = value.divide(new BigDecimal(100));
    }

    if (numerator == null) {
      numerator = value;
      begin = tokenBegin;
      end = tokenEnd;
      return false;
    } else {
      denominator = value;
      end = tokenEnd;
      numberType = NumberType.FRACTION;
      return true;
    }
  }

  /**
   * Tells the acceptor that there are no more tokens to pass to it. It responds with whether a
   * number was detected that has not return a true result from try token.
   *
   * @return true if there is an accepted token, false otherwise
   */
  public boolean finish() {
    if (numerator != null) {
      denominator = BigDecimal.ONE;
      numberType = NumberType.DECIMAL;
      return true;
    }
    return false;
  }
}
