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


import com.android.sched.util.codec.KeyValueCodec;
import com.android.sched.util.codec.KeyValueCodec.Entry;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.expression.BooleanExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link Boolean}
 */
public class BooleanPropertyId extends PropertyId<Boolean> {
  @Nonnull
  private static KeyValueCodec<Boolean> parser;

  @Nonnull
  public static BooleanPropertyId create(@Nonnull String name, @Nonnull String description) {
    return new BooleanPropertyId(name, description);
  }

  protected BooleanPropertyId(@Nonnull String name, @Nonnull String description) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public BooleanPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public BooleanPropertyId addDefaultValue (@Nonnull Boolean defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Nonnull
  public BooleanPropertyId addDefaultValue (boolean defaultValue) {
    super.addDefaultValue(Boolean.valueOf(defaultValue));

    return this;
  }

  @Override
  @Nonnull
  public BooleanPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Nonnull
  public BooleanExpression getValue() {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(BooleanPropertyId.this);
        }

        return checker.parse(BooleanPropertyId.this).booleanValue();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return formatPropertyName(BooleanPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return formatPropertyName(checker, BooleanPropertyId.this);
      }
    };
  }

  @Override
  @Nonnull
  public BooleanPropertyId makePrivate() {
    super.makePrivate();
    return this;
  }

  static {
    @SuppressWarnings("unchecked")
    Entry<Boolean>[] elements =
      new Entry[] {
        new Entry<Boolean>("true",  Boolean.TRUE),
        new Entry<Boolean>("yes",   Boolean.TRUE),
        new Entry<Boolean>("on",    Boolean.TRUE),
        new Entry<Boolean>("1",     Boolean.TRUE),
        new Entry<Boolean>("false", Boolean.FALSE),
        new Entry<Boolean>("no",    Boolean.FALSE),
        new Entry<Boolean>("off",   Boolean.FALSE),
        new Entry<Boolean>("0",     Boolean.FALSE)
    };

    parser = new KeyValueCodec<Boolean>(elements).ignoreCase();
  }
}
