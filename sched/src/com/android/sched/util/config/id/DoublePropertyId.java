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
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.DoubleExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link Double}
 */
public class DoublePropertyId extends PropertyId<Double> {
  @Nonnull
  public static DoublePropertyId create(@Nonnull String name, @Nonnull String description) {
    return new DoublePropertyId(
        name, description, new DoubleCodec());
  }

  protected DoublePropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull DoubleCodec parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public DoublePropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public DoublePropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public DoubleCodec getCodec() {
    return (DoubleCodec) super.getCodec();
  }

  @Nonnull
  public DoublePropertyId withMin(double min) {
    getCodec().setMin(min);

    return this;
  }

  @Nonnull
  public DoublePropertyId withMax(double max) {
    getCodec().setMax(max);

    return this;
  }

  @Nonnull
  public DoubleExpression getValue() {
    return new DoubleExpression() {
      @Override
      public double eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(DoublePropertyId.this);
        }

        return checker.parse(DoublePropertyId.this).doubleValue();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return formatPropertyName(DoublePropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return formatPropertyName(checker, DoublePropertyId.this);
      }
    };
  }
}
