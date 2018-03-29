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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Detects both decimal numbers using {@link DecimalNumberAcceptor} and english numbers using {@link
 * EnglishNumeralsAcceptor}.
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 1.0.0
 */
public class CombinedNumberDetector extends AbstractNumberDetector {

  private final FractionNumberDetector fractionNumberDetector;

  private final EnglishNumeralsAcceptor englishAcceptor;

  CombinedNumberDetector(FractionNumberDetector fractionNumberDetector,
      EnglishNumeralsAcceptor englishAcceptor) {
    this.fractionNumberDetector = fractionNumberDetector;
    this.englishAcceptor = englishAcceptor;
  }

  @Nonnull
  @Override
  public List<NumberResult> tryToken(String token, int tokenBegin, int tokenEnd) {
    List<NumberResult> results = fractionNumberDetector.tryToken(token, tokenBegin, tokenEnd);
    if (!results.isEmpty()) {
      englishAcceptor.reset();
      return results;
    }

    results = englishAcceptor.tryToken(token, tokenBegin, tokenEnd);
    if (!results.isEmpty()) {
      fractionNumberDetector.reset();
      return results;
    }

    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<NumberResult> finish() {
    List<NumberResult> results = fractionNumberDetector.finish();
    if (!results.isEmpty()) {
      englishAcceptor.reset();
      return results;
    }

    results = englishAcceptor.finish();
    if (!results.isEmpty()) {
      return results;
    }
    return Collections.emptyList();
  }
}
