/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sched.util.codec;

import com.android.sched.util.config.ConfigurationError;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link Double}
 */
public class DoubleCodec implements StringCodec<Double> {
  private double min;
  private double max;

  public DoubleCodec() {
    this.min = -Double.MAX_VALUE;
    this.max =  Double.MAX_VALUE;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public void setMax(double max) {
    this.max = max;
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a real number belonging to [" + min + " .. " + max + "]";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "number";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public Double checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      Double d = Double.valueOf(string);
      try {
        checkValue(context, d);
      } catch (CheckingException e) {
        throw new ParsingException(e);
      }

      return d;
    } catch (NumberFormatException e) {
      throw new ParsingException(
          "The value must be " + getUsage() + " but is '" + string + "'");
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Double d)
      throws CheckingException {
    double v = d.doubleValue();

    if (v < min || v > max) {
      throw new CheckingException(
          "The value must be " + getUsage() + " but is " + d);
    }
  }

  @Override
  @Nonnull
  public Double parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Double d) {
    return d.toString();
  }
}