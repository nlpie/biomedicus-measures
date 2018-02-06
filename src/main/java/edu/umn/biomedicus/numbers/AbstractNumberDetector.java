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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractNumberDetector {

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
  public abstract List<NumberResult> tryToken(String token, int tokenBegin, int tokenEnd);

  /**
   * Informs this instance that it is done being passed tokens, and to check if any of the tokens it
   * received previously create a number.
   *
   * @return {@code true} if the tokens it has received contain a number, {@code false} otherwise
   */
  public abstract List<NumberResult> finish();

  /**
   * Checks all the tokens in the list, returning any numbers.
   *
   * @param tokens the sequence of tokens to check
   * @return list of number results
   */
  public List<NumberResult> checkTokens(Iterable<Token> tokens) {
    ArrayList<NumberResult> results = new ArrayList<>();

    for (Token token : tokens) {
      results.addAll(tryToken(token.getText(), token.getBegin(), token.getEnd()));
    }
    results.addAll(finish());

    return results;
  }

  /**
   * Iteratively finds the numbers in an iterable sequence of tokens.
   *
   * @param tokens the sequence of tokens to check
   * @return iterable of number results
   */
  public Iterable<NumberResult> findNumbers(Iterable<Token> tokens) {
    return () -> new Iterator<NumberResult>() {
      NumberResult next = null;
      Iterator<NumberResult> current = null;
      Iterator<Token> tokenIt = tokens.iterator();

      {
        advance();
      }

      void advance() {
        if (current != null && current.hasNext()) {
          next = current.next();
        } else {
          next = null;
        }

        while (true) {
          if (!tokenIt.hasNext()) {
            current = finish().iterator();
            break;
          }
          Token nextToken = tokenIt.next();
          List<NumberResult> results = tryToken(nextToken.getText(), nextToken.getBegin(),
              nextToken.getEnd());
          if (!results.isEmpty()) {
            current = results.iterator();
            next = current.next();
            break;
          }
        }
      }

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public NumberResult next() {
        if (next == null) {
          throw new NoSuchElementException();
        }

        NumberResult next = this.next;
        advance();
        return next;
      }
    };
  }
}
