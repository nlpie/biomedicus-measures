/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

/**
 * A type of number.
 *
 * @since 1.0.0
 */
public enum NumberType {
  /**
   * An english numeral cardinal number.
   */
  CARDINAL,
  /**
   * An english numeral ordinal number.
   */
  ORDINAL,
  /**
   * An english numeral fraction.
   */
  FRACTION,
  /**
   * A decimal form number.
   */
  DECIMAL
}
