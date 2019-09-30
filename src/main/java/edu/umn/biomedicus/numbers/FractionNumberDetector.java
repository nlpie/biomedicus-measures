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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Uses {@link DecimalNumberAcceptor} to find instances of decimal numbers and decimal number
 * fractions.
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 2.0.0
 */
public class FractionNumberDetector extends AbstractNumberDetector {

  private final DecimalNumberAcceptor decimalNumberAcceptor;

  @Nullable
  private NumberResult numerator = null;

  @Nullable
  private NumberResult denominator = null;

  boolean sawSlash = false;

  FractionNumberDetector(
      DecimalNumberAcceptor decimalNumberAcceptor
  ) {
    this.decimalNumberAcceptor = decimalNumberAcceptor;
  }

  public FractionNumberDetector() {
    this(new DecimalNumberAcceptor());
  }

  /**
   * Resets the number detector to default state. {@link #tryToken(String, int, int)}
   * and {@link #finish()} do this automatically.
   */
  public void reset() {
    numerator = null;
    denominator = null;
    sawSlash = false;
  }

  /**
   * Advances the detector, returning any numbers that are confirmed by the token passed in.
   *
   * @param token token text
   * @param begin token begin offset
   * @param end token end offset
   * @return list of number results, if any, that are found because of the token passed in
   */
  @Nonnull
  @Override
  public List<NumberResult> tryToken(String token, int begin, int end) {
    NumberResult next = decimalNumberAcceptor.tryToken(token, begin, end);
    if (numerator == null) {
      numerator = next;
      if (numerator != null && numerator.getNumberType().equals(NumberType.ORDINAL)) {
        return swapAndReturnNumerator(null);
      }
    } else if (denominator == null) {
      if (sawSlash) {
        denominator = next;
        if (denominator == null) {
          List<NumberResult> result = swapAndReturnNumerator(null);
          sawSlash = false;
          return result;
        }
      } else if (token.length() == 1 && token.charAt(0) == '/') {
        sawSlash = true;
      } else {
        return swapAndReturnNumerator(next);
      }
    } else {
      // check if the previously parsed numerator and denominators are actually fractions
      List<NumberResult> result;
      if (token.length() == 1 && token.charAt(0) == '/' || denominator.getNumerator().intValue() == 0) {
        // not a fraction
        result = Arrays.asList(numerator, denominator);
      } else {
        result = buildFraction();
      }
      reset();
      return result;
    }
    return Collections.emptyList();
  }

  /**
   * Finishes the detector, returning any numbers that are confirmed by the end of the sequence of
   * tokens.
   *
   * @return list of number results, if any, that are found because of the token passed in
   */
  @Nonnull
  @Override
  public List<NumberResult> finish() {
    List<NumberResult> result;
    if (numerator == null) {
      result = Collections.emptyList();
    } else if (denominator == null) {
      result = swapAndReturnNumerator(null);
    } else {
      result = buildFraction();
    }
    reset();
    return result;
  }

  private List<NumberResult> buildFraction() {
    assert numerator != null : "this should only be called when numerator is not null";
    assert denominator != null : "this should only be called when denominator is not null";
    return Collections.singletonList(
        new NumberResult(numerator.getBegin(), denominator.getEnd(),
            numerator.getNumerator(), denominator.getNumerator(), NumberType.FRACTION)
    );
  }

  private List<NumberResult> swapAndReturnNumerator(@Nullable NumberResult replacement) {
    List<NumberResult> result = Collections.singletonList(numerator);
    numerator = replacement;
    return result;
  }


}
