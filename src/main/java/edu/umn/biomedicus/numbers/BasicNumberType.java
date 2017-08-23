/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

/**
 * A number type for a basic, single-word number component.
 *
 * @since 1.0.0
 */
enum BasicNumberType {
  /**
   * The words "ten", "twenty", "thirty", etc.
   */
  DECADE,
  /**
   * The words "eleven", "thirteen", "fourteen", etc.
   */
  TEEN,
  /**
   * The words "one", "two", "three", etc.
   */
  UNIT,
  /**
   * The words "hundred", "thousand", "million", "billion", etc.
   */
  MAGNITUDE
}
