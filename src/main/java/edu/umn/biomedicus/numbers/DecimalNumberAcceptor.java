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
import javax.annotation.Nullable;

/**
 * Finds decimal numbers in text. Also detects hybrid ordinals, like "1st" "2nd" "3rd", etc.
 * <pre>
 *   {@code
 * Iterator<Token> iterator = tokens.iterator();
 * while (iterator.hasNext()) {
 *   Token token = iterator.next();
 *
 * }
 * }
 * </pre>
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 2.0.0
 */
public class DecimalNumberAcceptor {
  /**
   * Parses any decimal numbers from the token text. After it has detected a decimal number it will
   * return true. The number may not necessarily, and will not in many cases contain the token that
   * was just passed.
   *
   * @param token text to parse
   * @param tokenBegin the begin index/identifier to get returned if the token is part of a number
   * @param tokenEnd the end index/identifier to get returned if the token is a part of a number
   * @return true if the token finalized any number in progress in this acceptor, false otherwise
   */
  @Nullable
  public NumberResult tryToken(CharSequence token, int tokenBegin, int tokenEnd) {
    boolean isOrdinal = false;
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
      return null;
    }

    int period = -1;
    for (int i = 1; i < token.length(); i++) {
      ch = token.charAt(i);
      if (ch == ',') {
        continue;
      }

      if (ch == '.') {
        period = digits.length();
      } else if (Character.isDigit(ch)) {
        digits.append(ch);
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
      }
    }

    if (digits.length() == 0) {
      return null;
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

    NumberType numberType = NumberType.DECIMAL;
    if (isOrdinal) {
      numberType = NumberType.ORDINAL;
    }
    return new NumberResult(tokenBegin, tokenEnd, value, BigDecimal.ONE, numberType);
  }
}
