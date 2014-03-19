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

package com.android.sched.util.config.id;

import com.android.sched.util.codec.DoubleCodec;
import com.android.sched.util.config.expression.BooleanExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link Double} for probabilities.
 */
public class ProbabilityPropertyId extends DoublePropertyId {
  @Nonnull
  public static ProbabilityPropertyId create(@Nonnull String name, @Nonnull String description) {
    return new ProbabilityPropertyId(name, description, new ProbabilityCodec());
  }

  protected ProbabilityPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull ProbabilityCodec parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public ProbabilityPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public ProbabilityPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public ProbabilityPropertyId withMin(double min) {
    assert checkRange(min);
    super.withMin(min);

    return this;
  }

  @Override
  @Nonnull
  public ProbabilityPropertyId withMax(double max) {
    assert checkRange(max);
    super.withMax(max);

    return this;
  }

  private boolean checkRange(double value) {
    return value >= 0 && value <= 1;
  }

  /**
   * This {@code StringCodec} is used to create an instance of {@link Double} for probability value
   */
  protected static class ProbabilityCodec extends DoubleCodec {
    public ProbabilityCodec() {
      setMin(0.0);
      setMax(1.0);
    }

    @Override
    @Nonnull
    public String getUsage() {
      return "a probability belonging to [0 (never) .. 1 (always)]";
    }
  }
}
