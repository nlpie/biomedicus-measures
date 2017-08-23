/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
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
