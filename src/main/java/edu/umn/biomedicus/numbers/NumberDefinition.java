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

/**
 * Information about the number that a specific numeral word represents.
 *
 * @since 1.0.0
 */
class NumberDefinition {

  private final int value;

  private final BasicNumberType basicNumberType;

  NumberDefinition(int value, BasicNumberType basicNumberType) {
    this.value = value;
    this.basicNumberType = basicNumberType;
  }

  /**
   * The integer value of the number.
   *
   * @return int value of this number
   */
  public int getValue() {
    return value;
  }

  /**
   * The number type.
   *
   * @return the basic number type associated with this number.
   */
  public BasicNumberType getBasicNumberType() {
    return basicNumberType;
  }
}
