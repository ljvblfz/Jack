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


import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.LongExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link Long}
 */
public class LongPropertyId extends PropertyId<Long> {
  @Nonnull
  public static LongPropertyId create(@Nonnull String name, @Nonnull String description) {
    return new LongPropertyId(
        name, description, new LongCodec(Long.MIN_VALUE, Long.MAX_VALUE));
  }

  protected LongPropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull LongCodec parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public LongPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public LongPropertyId addDefaultValue (@Nonnull Long defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Nonnull
  public LongPropertyId addDefaultValue (@Nonnull long defaultValue) {
    super.addDefaultValue(Long.valueOf(defaultValue));

    return this;
  }

  @Override
  @Nonnull
  public LongPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public LongCodec getCodec() {
    return (LongCodec) super.getCodec();
  }

  @Nonnull
  public LongPropertyId withMin(long min) {
    getCodec().setMin(min);

    return this;
  }

  @Nonnull
  public LongPropertyId withMax(long max) {
    getCodec().setMax(max);

    return this;
  }

  @Nonnull
  public LongExpression getValue() {
    return new LongExpression() {
      @Override
      public long eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(LongPropertyId.this);
        }

        return checker.parse(LongPropertyId.this).longValue();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return formatPropertyName(LongPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return formatPropertyName(checker, LongPropertyId.this);
      }
    };
  }
}
