/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.numbers;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Access point for number related functions.
 *
 * @since 1.0.0
 */
public class Numbers {

  /**
   * Creates a combined number detector for both English numerals and decimal numbers using the
   * supplied number model.
   *
   * @param numberModel the number model to supply to the English numeral detector
   * @return newly created combined number detector
   */
  public CombinedNumberDetector createNumberDetector(NumberModel numberModel) {
    return new CombinedNumberDetector(new DecimalNumberAcceptor(),
        EnglishNumeralsAcceptor.create(numberModel));
  }

  /**
   * Creates a factory for creating new number detectors.
   *
   * @param nrnumPath the path to the SPECIALIST NRNUM file containing number definitions.
   * @param nrvarPath the path to the SPECIALIST NRVAR file containing number variants.
   * @return a factory class used to create detectors
   * @throws IOException if there are any errors reading the model fies.
   */
  public static DetectorFactory createFactory(Path nrnumPath, Path nrvarPath) throws IOException {
    return new DetectorFactory(NumberModel.createNumberModel(nrnumPath, nrvarPath));
  }

  /**
   * A factory object to create new instances of {@link EnglishNumeralsAcceptor} and {@link
   * CombinedNumberDetector}.
   *
   * @since 1.0.0
   */
  public static class DetectorFactory {

    private final NumberModel numberModel;

    DetectorFactory(NumberModel numberModel) {
      this.numberModel = numberModel;
    }

    /**
     * Creates an acceptor for English numerals.
     *
     * @return a new acceptor for English numerals.
     */
    public EnglishNumeralsAcceptor createEnglishNumeralsAcceptor() {
      return EnglishNumeralsAcceptor.create(numberModel);
    }

    /**
     * Creates an acceptor for decimal numbers.
     *
     * @return a new acceptor for decimal numbers.
     */
    public DecimalNumberAcceptor createDecimalNumberAcceptor() {
      return new DecimalNumberAcceptor();
    }

    /**
     * Creates a combined acceptor for both English numerals and decimal numbers.
     *
     * @return a newly created acceptor for both English numerals and decimal numbers.
     */
    public CombinedNumberDetector createCombinedNumberDetector() {
      return new CombinedNumberDetector(new DecimalNumberAcceptor(),
          createEnglishNumeralsAcceptor());
    }
  }
}