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

import java.io.IOException;
import java.nio.file.Path;

/**
 * Access point for number related functions.
 *
 * @since 1.0.0
 */
public class Numbers {

  private Numbers() {
    throw new UnsupportedOperationException("Instantiation of utility class");
  }

  /**
   * Creates a combined number detector for both English numerals and decimal numbers using the
   * supplied number model.
   *
   * @param numberModel the number model to supply to the English numeral detector
   * @return newly created combined number detector
   */
  public static CombinedNumberDetector createNumberDetector(
      NumberModel numberModel
  ) {
    return new CombinedNumberDetector(new FractionNumberDetector(),
        EnglishNumeralsAcceptor.create(numberModel));
  }

  /**
   * Creates a new combined number detector using a number model from the classpath.
   *
   * @return newly created combined number detector
   * @throws IOException if we fail to load the data from the classpath
   */
  public static CombinedNumberDetector createNumberDetector() throws IOException {
    NumberModel numberModel = NumberModel.createNumberModel();
    return new CombinedNumberDetector(new FractionNumberDetector(),
        EnglishNumeralsAcceptor.create(numberModel));
  }

  /**
   * Creates a combined number detector for both English numerals and decimal numbers using the
   * supplied paths for number model files.
   *
   * @param nrnumPath the path to the SPECIALIST LEXICON NRNUM file.
   * @param nrvarPath the path to the SPECIALIST LEXICON NRVAR file.
   * @return newly created combined number detector
   * @throws IOException if there are any exceptions thrown loading the number files
   */
  public static CombinedNumberDetector createNumberDetector(
      Path nrnumPath,
      Path nrvarPath
  )
      throws IOException {
    return new CombinedNumberDetector(new FractionNumberDetector(),
        EnglishNumeralsAcceptor.create(NumberModel.createNumberModel(nrnumPath, nrvarPath)));
  }

  /**
   * Creates a factory for creating new number detectors.
   *
   * @param nrnumPath the path to the SPECIALIST NRNUM file containing number definitions.
   * @param nrvarPath the path to the SPECIALIST NRVAR file containing number variants.
   * @return a factory class used to create detectors
   * @throws IOException if there are any errors reading the model fies.
   */
  public static DetectorFactory createFactory(
      Path nrnumPath,
      Path nrvarPath
  ) throws IOException {
    return new DetectorFactory(NumberModel.createNumberModel(nrnumPath, nrvarPath));
  }

  /**
   * Creates a factory using NRNUM and NRVAR from the classpath.
   *
   * @return a factory class used to create detectors
   * @throws IOException if we fail to read the model from the classpath
   */
  public static DetectorFactory createFactory() throws IOException {
    return new DetectorFactory(NumberModel.createNumberModel());
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
      return new CombinedNumberDetector(new FractionNumberDetector(),
          createEnglishNumeralsAcceptor());
    }

    /**
     * Creates fraction number detector.
     *
     * @return a newly created detector for decimals and fractions of decimals
     */
    public FractionNumberDetector createFractionNumberDetector() {
      return new FractionNumberDetector();
    }
  }
}
