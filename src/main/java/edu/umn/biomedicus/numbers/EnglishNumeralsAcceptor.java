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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Finds english numerals in text, like "one", "five eighths", or "three hundred billion and six".
 * Will look for basic cardinal and ordinal numbers as well as fractions.
 *
 * <p>It is not safe to use an instance of this class from multiple threads at once, use multiple
 * instances for concurrency.</p>
 *
 * @since 1.0.0
 */
public class EnglishNumeralsAcceptor extends AbstractNumberDetector {

  private final NonFractionAcceptor nonFractionAcceptor;

  @Nullable
  private BigDecimal numerator;

  @Nullable
  private BigDecimal denominator;

  private int begin;

  private int end;

  @Nullable
  private NumberType numberType;

  private int andHalf = 0;

  EnglishNumeralsAcceptor(NonFractionAcceptor nonFractionAcceptor) {
    this.nonFractionAcceptor = nonFractionAcceptor;
  }

  /**
   * Returns a new EnglishNumeralsAcceptor.
   *
   * @param numberModel the number model to use
   * @return newly create acceptor
   */
  public static EnglishNumeralsAcceptor create(NumberModel numberModel) {
    return new EnglishNumeralsAcceptor(new NonFractionAcceptor(numberModel,
        new BasicNumberAcceptor(numberModel)));
  }

  /**
   * Resets this number acceptor to its default state.
   */
  public void reset() {
    numerator = null;
    denominator = null;
    nonFractionAcceptor.reset();
    numberType = null;
    andHalf = 0;
  }

  @Nonnull
  @Override
  public List<NumberResult> tryToken(@Nonnull String token, int tokenBegin, int tokenEnd) {
    NumberResult previous = null;
    for (int i = 0; i < 2; i++) {
      if (numerator == null) {
        if (nonFractionAcceptor.tryToken(token, tokenBegin, tokenEnd)) {
          numerator = nonFractionAcceptor.value;
          begin = nonFractionAcceptor.begin;
          end = nonFractionAcceptor.end;
          nonFractionAcceptor.reset();
          nonFractionAcceptor.setDenominator();

          if (nonFractionAcceptor.isOrdinal) {
            numberType = NumberType.ORDINAL;
            NumberResult current = new NumberResult(begin, end, numerator, BigDecimal.ONE,
                numberType);
            reset();
            return previous != null ? Arrays.asList(previous, current) : Collections.singletonList(current);
          }

          numberType = NumberType.CARDINAL;

          if (nonFractionAcceptor.consumedLastToken) {
            return previous != null ? Collections.singletonList(previous) : Collections.emptyList();
          }
        } else {
          return previous != null ? Collections.singletonList(previous) : Collections.emptyList();
        }
      }

      if (token.equals("-")) {
        return previous != null ? Collections.singletonList(previous) : Collections.emptyList();
      } else if (andHalf == 1 && token.equalsIgnoreCase("a")) {
        andHalf = 2;
      } else if (andHalf == 2 && token.equalsIgnoreCase("half")) {
        denominator = BigDecimal.valueOf(2);
        numerator = numerator.multiply(denominator).add(BigDecimal.ONE);
        numberType = NumberType.FRACTION;
        end = tokenEnd;
        NumberResult halfResult = stateToResult();
        reset();
        return Collections.singletonList(halfResult);
      } else if (token.equalsIgnoreCase("and")) {
        andHalf = 1;
      } else if (nonFractionAcceptor.tryToken(token, tokenBegin, tokenEnd)) {
        denominator = nonFractionAcceptor.value;
        end = nonFractionAcceptor.end;
        numberType = NumberType.FRACTION;

        previous = stateToResult();

        if (nonFractionAcceptor.consumedLastToken) {
          reset();
          return Collections.singletonList(previous);
        } else {
          continue;
        }
      } else {
        if (!nonFractionAcceptor.inProgress()) {
          denominator = BigDecimal.ONE;
          if (previous != null) {
            throw new IllegalStateException("Losing a value before returning it.");
          }
          previous = stateToResult();
          reset();
        }

        if (!nonFractionAcceptor.consumedLastToken) {
          continue;
        }
      }
      return previous != null ? Collections.singletonList(previous) : Collections.emptyList();
    }
    return previous != null ? Collections.singletonList(previous) : Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<NumberResult> finish() {
    if (numerator == null) {
      if (nonFractionAcceptor.finish()) {
        numerator = nonFractionAcceptor.value;
        begin = nonFractionAcceptor.begin;
        end = nonFractionAcceptor.end;
        if (nonFractionAcceptor.isOrdinal) {
          numberType = NumberType.ORDINAL;
        } else {
          numberType = NumberType.CARDINAL;
        }
        return Collections.singletonList(stateToResult());
      }
    } else {
      if (nonFractionAcceptor.finish()) {
        denominator = nonFractionAcceptor.value;
        end = nonFractionAcceptor.end;
        numberType = NumberType.FRACTION;
        return Collections.singletonList(stateToResult());
      }
    }
    return Collections.emptyList();
  }

  /**
   * Detects basic cardinal numbers.
   */
  static class BasicNumberAcceptor {

    private enum State {
      DECADE,
      DECADE_HYPHEN,
      NONE
    }

    private enum Type {
      UNIT,
      TEEN,
      DECADE,
      DECADE_UNIT
    }

    final NumberModel numberModel;

    int value;

    int begin;

    int end;

    State state;

    Type type;

    boolean consumedLastToken;

    boolean canBeDenominator;

    boolean isDenominator;

    boolean isOrdinal;

    BasicNumberAcceptor(NumberModel numberModel) {
      this.numberModel = numberModel;
      reset();
    }

    void reset() {
      value = -1;
      begin = -1;
      end = -1;
      state = State.NONE;
      type = null;
      consumedLastToken = false;
      canBeDenominator = false;
      isDenominator = false;
      isOrdinal = false;
    }

    boolean tryToken(String token, int tokenStart, int tokenEnd) {
      NumberDefinition numberDefinition = null;
      if (canBeDenominator) {
        numberDefinition = numberModel.getDenominator(token);
        if (numberDefinition != null) {
          isDenominator = true;
        }
      }

      if (numberDefinition == null) {
        numberDefinition = numberModel.getNumberDefinition(token);
      }

      if (numberDefinition == null) {
        numberDefinition = numberModel.getOrdinal(token);
        if (numberDefinition != null) {
          isOrdinal = true;
        }
      }

      switch (state) {
        case NONE:
          if (numberDefinition != null) {
            switch (numberDefinition.getBasicNumberType()) {
              case TEEN:
                value = numberDefinition.getValue();
                begin = tokenStart;
                end = tokenEnd;
                type = Type.TEEN;
                consumedLastToken = true;
                return true;
              case UNIT:
                value = numberDefinition.getValue();
                begin = tokenStart;
                end = tokenEnd;
                type = Type.UNIT;
                consumedLastToken = true;
                return true;
              case DECADE:
                state = State.DECADE;
                value = numberDefinition.getValue();
                begin = tokenStart;
                end = tokenEnd;
                type = Type.DECADE;
                consumedLastToken = true;
                if (isDenominator || isOrdinal) {
                  return true;
                }
                break;
            }
          }
          break;
        case DECADE:
          if ("-".equals(token)) {
            state = State.DECADE_HYPHEN;
            return false;
          }
        case DECADE_HYPHEN:
          if (numberDefinition != null) {
            if (numberDefinition.getBasicNumberType() == BasicNumberType.UNIT) {
              value = value + numberDefinition.getValue();
              end = tokenEnd;
              type = Type.DECADE_UNIT;
              consumedLastToken = true;
              return true;
            }
          }
          consumedLastToken = false;
          return true;
      }

      return false;
    }

    boolean finish() {
      switch (state) {
        case DECADE_HYPHEN:
        case DECADE:
          return true;
      }
      return false;
    }
  }

  private NumberResult stateToResult() {
    return new NumberResult(begin, end, numerator, denominator, numberType);
  }

  /**
   * Finds any english numeral numerators or denominators.
   */
  static class NonFractionAcceptor {

    enum State {
      NONE,
      HAS_BASIC,
      RANK_01,
      PAST_FIRST_PART,
      POST_MAGNITUDE
    }


    private final NumberModel numberModel;

    private final BasicNumberAcceptor basicNumberAcceptor;

    private State state;

    @Nullable
    BigDecimal value;

    private int valueBuilder = 0;

    int begin = -1;

    int end = -1;

    private boolean canBeDenominator;

    boolean isDenominator;

    boolean isOrdinal;

    boolean consumedLastToken;

    NonFractionAcceptor(NumberModel numberModel, BasicNumberAcceptor basicNumberAcceptor) {
      this.basicNumberAcceptor = basicNumberAcceptor;
      this.numberModel = numberModel;
      reset();
    }

    private void reset() {
      state = State.NONE;
      value = null;
      valueBuilder = -1;
      begin = -1;
      end = -1;
      consumedLastToken = false;
      canBeDenominator = false;
      isDenominator = false;
      isOrdinal = false;

      basicNumberAcceptor.reset();
    }

    boolean tryToken(String token, int tokenBegin, int tokenEnd) {
      NumberDefinition numberDefinition = numberModel.getNumberDefinition(token);

      switch (state) {
        case NONE:
          if (value != null && "and".equalsIgnoreCase(token)) {
            return false;
          }

          boolean basicTry = basicNumberAcceptor.tryToken(token, tokenBegin, tokenEnd);
          consumedLastToken = basicNumberAcceptor.consumedLastToken;
          if (basicTry) {
            state = State.HAS_BASIC;
            if (value == null) {
              begin = basicNumberAcceptor.begin;
            }
            end = basicNumberAcceptor.end;
            valueBuilder = basicNumberAcceptor.value;
            if (basicNumberAcceptor.isDenominator || basicNumberAcceptor.isOrdinal
                || !basicNumberAcceptor.consumedLastToken) {
              value = BigDecimal.valueOf(valueBuilder);
              return true;
            }
          } else if (value != null) {
            break;
          }
          return false;
        case HAS_BASIC:
          if ("hundred".equalsIgnoreCase(token)) {
            state = State.RANK_01;
            valueBuilder = valueBuilder * 100;
            end = tokenEnd;
            basicNumberAcceptor.reset();
            return false;
          }
          if ("hundredth".equalsIgnoreCase(token)) {
            if (canBeDenominator) {
              isDenominator = true;
            } else {
              isOrdinal = true;
            }
            valueBuilder = valueBuilder * 100;
            end = tokenEnd;
            basicNumberAcceptor.reset();
            value = BigDecimal.valueOf(valueBuilder);
            consumedLastToken = true;
            return true;
          }
          if ("hundredths".equalsIgnoreCase(token)) {
            isDenominator = true;
            valueBuilder = valueBuilder * 100;
            end = tokenEnd;
            basicNumberAcceptor.reset();
            value = BigDecimal.valueOf(valueBuilder);
            consumedLastToken = true;
            return true;
          }
          break;
        case RANK_01:
          if ("and".equalsIgnoreCase(token)) {
            return false;
          }

          if (basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE) {
            if (basicNumberAcceptor.tryToken(token, tokenBegin, tokenEnd)) {
              valueBuilder += basicNumberAcceptor.value;
              end = basicNumberAcceptor.end;
              state = State.PAST_FIRST_PART;
            }
          } else {
            basicNumberAcceptor.tryToken(token, tokenBegin, tokenEnd);
            if (basicNumberAcceptor.state == BasicNumberAcceptor.State.NONE) {
              break;
            }
          }
          return false;
        default:
      }

      // if we fall through to here look for a magnitude

      if (numberDefinition != null) {
        if (BasicNumberType.MAGNITUDE == numberDefinition.getBasicNumberType()) {
          if (value != null) {
            value = value.add(BigDecimal.valueOf(1000).pow(numberDefinition.getValue())
                .multiply(BigDecimal.valueOf(valueBuilder)));
          } else {
            value = BigDecimal.valueOf(1000).pow(numberDefinition.getValue())
                .multiply(BigDecimal.valueOf(valueBuilder));
          }
          valueBuilder = 0;
          end = tokenEnd;
          state = State.NONE;
          return false;
        }
      }

      if (canBeDenominator) {
        numberDefinition = numberModel.getDenominator(token);
        if (numberDefinition != null
            && numberDefinition.getBasicNumberType() == BasicNumberType.MAGNITUDE) {
          if (value != null) {
            value = value.add(BigDecimal.valueOf(1000).pow(numberDefinition.getValue())
                .multiply(BigDecimal.valueOf(valueBuilder)));
          } else {
            value = BigDecimal.valueOf(1000).pow(numberDefinition.getValue())
                .multiply(BigDecimal.valueOf(valueBuilder));
          }
          end = tokenEnd;
          consumedLastToken = true;
          return true;
        }
      }

      // if we fall through to here we've reached the end of the number

      if (value == null) {
        value = BigDecimal.valueOf(valueBuilder);
      } else {
        value = value.add(BigDecimal.valueOf(valueBuilder));
      }

      consumedLastToken = false;
      return true;
    }

    boolean finish() {
      switch (state) {
        case NONE:
          if (basicNumberAcceptor.finish()) {
            state = State.HAS_BASIC;
            if (value == null) {
              begin = basicNumberAcceptor.begin;
            }
            end = basicNumberAcceptor.end;
            valueBuilder = basicNumberAcceptor.value;
          }
          break;
        case HAS_BASIC:
        case RANK_01:
          if (basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE) {
            if (basicNumberAcceptor.finish()) {
              valueBuilder += basicNumberAcceptor.value;
              end = basicNumberAcceptor.end;
              state = State.PAST_FIRST_PART;
            }
          }
        default:
      }
      if (value == null) {
        if (state == State.NONE) {
          return false;
        }
        value = BigDecimal.valueOf(valueBuilder);
      } else {
        value = value.add(BigDecimal.valueOf(valueBuilder));
      }

      return true;
    }

    boolean inProgress() {
      return state != State.NONE || basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE;
    }

    void setDenominator() {
      this.canBeDenominator = true;
      basicNumberAcceptor.canBeDenominator = true;
    }
  }
}
