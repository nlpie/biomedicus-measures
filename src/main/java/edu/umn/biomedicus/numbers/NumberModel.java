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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Stores information about English numeral words and the mappings from those words to their decimal
 * forms. It is used by other classes in this package to perform work.
 *
 * <p>This class is immutable so it is thread-safe.</p>
 *
 * @since 1.0.0
 */
public class NumberModel {

  private final Map<String, NumberDefinition> numbers;

  private final Map<String, NumberDefinition> ordinals;

  private final Map<String, NumberDefinition> denominators;

  private NumberModel(Map<String, NumberDefinition> numbers,
      Map<String, NumberDefinition> ordinals,
      Map<String, NumberDefinition> denominators) {
    this.numbers = numbers;
    this.ordinals = ordinals;
    this.denominators = denominators;
  }

  /**
   * Loads the numbers and their variants from SPECIALIST LEXICON NRNUM and NRVAR file, using
   * versions that are on the classpath.
   *
   * @return newly created number model instance.
   * @throws IOException if there are any issues loading the files.
   */
  public static NumberModel createNumberModel() throws IOException {
    Map<String, NumberDefinition> numbers = new HashMap<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        NumberModel.class.getClassLoader().getResourceAsStream(
            "edu/umn/biomedicus/measures/NRNUM")))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] split = line.split("\\|");
        String word = split[1];
        BasicNumberType basicNumberType = typeFromString(split[2]);

        int value;
        if (basicNumberType == BasicNumberType.MAGNITUDE) {
          value = Integer.valueOf(split[5]);
        } else {
          value = Integer.valueOf(split[3]);
        }

        NumberDefinition numberDefinition = new NumberDefinition(value, basicNumberType);
        numbers.put(word, numberDefinition);
      }
    }

    Map<String, NumberDefinition> ordinals = new HashMap<>();
    Map<String, NumberDefinition> denominators = new HashMap<>();


    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        NumberModel.class.getClassLoader().getResourceAsStream(
            "edu/umn/biomedicus/measures/NRVAR")))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] split = line.split("\\|");
        String word = split[0];
        String types = split[2];
        String norm = split[3];
        NumberDefinition numberDefinition = numbers.get(norm);
        if (types.contains("ordinal")) {
          ordinals.put(word, numberDefinition);
        }
        if (types.contains("denominator")) {
          denominators.put(word, numberDefinition);
        }
      }
    }

    return new NumberModel(numbers, ordinals, denominators);
  }

  /**
   * Loads the numbers and their variants from SPECIALIST LEXICON NRNUM and NRVAR file.
   *
   * @param nrnumPath path to the specialist lexicon NRNUM file.
   * @param nrvarPath path to the specialist lexicon NRVAR file.
   * @return newly created number model instance.
   * @throws IOException if there are any issues loading the files.
   */
  public static NumberModel createNumberModel(Path nrnumPath, Path nrvarPath) throws IOException {
    Map<String, NumberDefinition> numbers = new HashMap<>();

    Files.lines(nrnumPath).forEach(line -> {
      String[] split = line.split("\\|");
      String word = split[1];
      BasicNumberType basicNumberType = typeFromString(split[2]);

      int value;
      if (basicNumberType == BasicNumberType.MAGNITUDE) {
        value = Integer.valueOf(split[5]);
      } else {
        value = Integer.valueOf(split[3]);
      }

      NumberDefinition numberDefinition = new NumberDefinition(value, basicNumberType);
      numbers.put(word, numberDefinition);
    });

    Map<String, NumberDefinition> ordinals = new HashMap<>();
    Map<String, NumberDefinition> denominators = new HashMap<>();

    Files.lines(nrvarPath).forEach(line -> {
      String[] split = line.split("\\|");
      String word = split[0];
      String types = split[2];
      String norm = split[3];
      NumberDefinition numberDefinition = numbers.get(norm);
      if (types.contains("ordinal")) {
        ordinals.put(word, numberDefinition);
      }
      if (types.contains("denominator")) {
        denominators.put(word, numberDefinition);
      }
    });

    return new NumberModel(numbers, ordinals, denominators);
  }

  @Nullable
  NumberDefinition getNumberDefinition(String word) {
    return numbers.get(word.toLowerCase());
  }

  @Nullable
  NumberDefinition getOrdinal(String word) {
    return ordinals.get(word.toLowerCase());
  }

  @Nullable
  NumberDefinition getDenominator(String word) {
    return denominators.get(word.toLowerCase());
  }

  private static BasicNumberType typeFromString(String st) {
    switch (st) {
      case "unit":
        return BasicNumberType.UNIT;
      case "teen":
        return BasicNumberType.TEEN;
      case "decade":
        return BasicNumberType.DECADE;
      case "magnitude":
        return BasicNumberType.MAGNITUDE;
    }
    throw new IllegalStateException("Unrecognized number type: " + st);
  }
}
