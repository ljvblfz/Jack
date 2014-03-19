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
import com.android.sched.util.config.expression.BooleanExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link Integer}
 */
public class IntegerPropertyId extends LongPropertyId {
  @Nonnull
  public static IntegerPropertyId create(@Nonnull String name, @Nonnull String description) {
    return new IntegerPropertyId(
        name, description, new LongCodec(Integer.MIN_VALUE, Integer.MAX_VALUE));
  }

  protected IntegerPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull LongCodec parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public IntegerPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public IntegerPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public IntegerPropertyId withMin(long min) {
    assert checkRange(min);
    super.withMin(min);

    return this;
  }

  @Override
  @Nonnull
  public IntegerPropertyId withMax(long max) {
    assert checkRange(max);
    super.withMax(max);

    return this;
  }

  private boolean checkRange(long value) {
    return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
  }
}
