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

package edu.umn.biomedicus.measures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Recognizes token sequences that are units of measure. Maps the units of measure to their Unified
 * Code of Units of Measurement code.
 *
 * <br>Usage:
 * <pre>
 *   {@code
for(String word : sentence) {
  Optional<Result> potentialResult = unitRecognizer.advance(word, index, index + word.length());
  if (potentialResult.isPresent()) {
    Result result = potentialResult.get();
    // do stuff with result.
  }
  index += word.length();
}
Optional<Result> potentialResult = unitRecognizer.finish();
if (potentialResult.isPresent()) {
  Result result = potentialResult.get();
  int begin = result.getBegin();
  int end = result.getEnd();
}
 *   }
 * </pre>
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 1.0.0
 */
public class UnitRecognizer {

  private final Map<String, String> unitOfMeasureMap;

  private int start = -1;

  private int end = -1;

  private StringBuilder codeBuilder = new StringBuilder();

  /**
   * Constructs a units recognizer.
   *
   * @param unitOfMeasureMap a map from units to their UCUM code.
   */
  public UnitRecognizer(Map<String, String> unitOfMeasureMap) {
    this.unitOfMeasureMap = unitOfMeasureMap;
  }

  /**
   * Whether or not the unit recognizer has begun recognizing a unit.
   *
   * @return true if it has begun, false otherwise
   */
  public boolean inProgress() {
    return start != -1;
  }

  /**
   * Advances the recognizer, detecting if tokens passed to it have any units of measurement.
   *
   * @param token the token to pass, should already be lowercased
   * @param begin the begin index of the token
   * @param end the end index of the token
   * @return an optional result will be present when a unit of measurement was detected.
   */
  public Optional<Result> advance(CharSequence token, int begin, int end) {
    String lowercased = token.toString().toLowerCase();

    return advanceLowercased(lowercased, begin, end);
  }

  /**
   * Advances the recognizer, detecting if tokens passed to it have any units of measurement. This
   * version of the {@link #advance(CharSequence, int, int)} expects tokens to have been previously
   * converted to lowercase.
   *
   * @param token the token to pass, should already be lowercased
   * @param begin the begin index of the token
   * @param end the end index of the token
   * @return an optional result will be present when a unit of measurement was detected.
   */
  public Optional<Result> advanceLowercased(String token, int begin, int end) {
    if (unitOfMeasureMap.containsKey(token)) {
      start = begin;
      this.end = end;
    } else if (!token.equals("/") && !token.equals("per")) {
      if (begin != -1) {
        Result result = new Result();
        result.begin = start;
        result.end = this.end;
        result.code = codeBuilder.toString();

        // consume the current token, since units will never be discovered on the first token
        // passed to the unit recognizer
        reset();
        advanceLowercased(token, begin, end);

        return Optional.of(result);
      }
    }

    return Optional.empty();
  }

  private void reset() {
    start = -1;
    end = -1;
    codeBuilder = new StringBuilder();
  }

  /**
   * Tests the tokens that have been passed to the recognizer and are in progress.
   *
   * @return an optional result will be present when a unit of measurement was detected.
   */
  public Optional<Result> finish() {
    if (start != -1) {
      Result result = new Result();
      result.begin = start;
      result.end = this.end;
      result.code = codeBuilder.toString();

      reset();

      return Optional.of(result);
    }
    return Optional.empty();
  }

  /**
   * A simple test to determine if the string/word/token is a unit of measurement.
   *
   * @param string the string to test
   * @return true if the string is a unit of measurement, false if it is not
   */
  public boolean isUnitOfMeasureWord(String string) {
    return unitOfMeasureMap.containsKey(string.toLowerCase());
  }

  /**
   * A simple test to determine if the string/word/token is a unit of measurement. This version of
   * {@link #isUnitOfMeasureWord(String)} expects the word to have already been converted to a
   * lowercase format.
   *
   * @param lowercase the lowercased version of the string to test.
   * @return true if the string is a unit of measurement, false if it is not.
   */
  public boolean isUnitOfMeasureWordLowercased(String lowercase) {
    return unitOfMeasureMap.containsKey(lowercase);
  }

  /**
   * Creates a {@link Factory} to create new Unit Recognizers, loading data from the file at the
   * specified path.
   *
   * @param unitsOfMeasureFile file to load units of measure and their codes from
   * @return factory object used to create unit recognizers
   * @throws IOException if there is an error loading the factory
   */
  public static Factory createFactory(Path unitsOfMeasureFile) throws IOException {
    try (BufferedReader unitsReader = Files.newBufferedReader(unitsOfMeasureFile,
        StandardCharsets.UTF_8)) {
      return createFactory(unitsReader);
    }
  }

  /**
   * Creates a {@link Factory}, loading the units and subjects from their default files.
   *
   * @return newly initialized factory
   * @throws IOException if the data fails to load.
   */
  public static Factory createFactory() throws IOException {
    ClassLoader classLoader = UnitRecognizer.class.getClassLoader();
    try (BufferedReader unitsReader = new BufferedReader(new InputStreamReader(
        classLoader.getResourceAsStream("edu/umn/biomedicus/measures/unitsOfMeasure.txt")))
    ) {
      return createFactory(unitsReader);
    }
  }

  private static Factory createFactory(BufferedReader unitsReader)
      throws IOException {
    Map<String, String> unitsOfMeasureMap = new HashMap<>();
    Map<String, String> subjectsMap = new HashMap<>();

    Pattern split = Pattern.compile(":");

    String line;
    while ((line = unitsReader.readLine()) != null) {
      String[] splits = split.split(line);
      unitsOfMeasureMap.put(splits[0], splits[1]);
    }

    return new Factory(unitsOfMeasureMap);
  }

  /**
   * A factory class that can be used to provided multiple independent unit of measure recognizers.
   */
  public static class Factory {

    private final Map<String, String> unitOfMeasureMap;

    Factory(Map<String, String> unitOfMeasureMap) {
      this.unitOfMeasureMap = unitOfMeasureMap;
    }

    /**
     * Creates a new independent unit recognizer.
     *
     * @return newly created unit recognizer instance
     */
    public UnitRecognizer create() {
      return new UnitRecognizer(unitOfMeasureMap);
    }
  }

  /**
   * The result when the unit detector finds a unit.
   */
  public static final class Result {
    private int begin;
    private int end;
    private String code;

    Result() {

    }

    /**
     * The begin index of the unit.
     * @return the integer begin index / identifier
     */
    public int getBegin() {
      return begin;
    }

    /**
     * The end index of the unit.
     *
     * @return the integer end index / identifier
     */
    public int getEnd() {
      return end;
    }

    /**
     * The UCUM code for the unit.
     *
     * @return the UCUM code for the unit of measure
     */
    public String getCode() {
      return code;
    }
  }
}
