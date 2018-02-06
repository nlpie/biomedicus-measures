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

/**
 * A token as used by the number detector.
 *
 * @since 2.0.0
 */
public class Token {
  private final int begin;
  private final int end;
  private final String text;

  /**
   * Creates a new token type.
   *
   * @param begin the begin offset of the token
   * @param end the end offset of the token
   * @param text the text contained in the token
   */
  public Token(int begin, int end, String text) {
    this.begin = begin;
    this.end = end;
    this.text = text;
  }

  /**
   * The begin of the token
   *
   * @return integer offset of the first character in the token
   */
  public int getBegin() {
    return begin;
  }

  /**
   * The end of the token
   *
   * @return integer offset of the last character in the token
   */
  public int getEnd() {
    return end;
  }

  /**
   * The text of the token
   *
   * @return a string of the token's text
   */
  public String getText() {
    return text;
  }
}
