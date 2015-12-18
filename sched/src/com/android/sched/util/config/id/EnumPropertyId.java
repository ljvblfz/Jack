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

import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@code Enum}
 *
 * @param <T> the type of the enum
 */
public class EnumPropertyId<T extends Enum<T>> extends PropertyId<T> {
  @Nonnull
  public static <T extends Enum<T>> EnumPropertyId<T> create(@Nonnull String name,
      @Nonnull String description, @Nonnull Class<T> type, @Nonnull T[] values) {
    return new EnumPropertyId<T>(name, description, new EnumCodec<T>(type, values));
  }

  protected EnumPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull EnumCodec<T> parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public EnumPropertyId<T> addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public EnumPropertyId<T> addDefaultValue (@Nonnull T defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public EnumPropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public EnumCodec<T> getCodec() {
    return (EnumCodec<T>) super.getCodec();
  }

  @Nonnull
  public EnumPropertyId<T> ignoreCase() {
    getCodec().ignoreCase();

    return this;
  }

  @Nonnull
  public EnumPropertyId<T> sorted() {
    getCodec().sorted();

    return this;
  }

  @Override
  @Nonnull
  public EnumPropertyId<T> addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Nonnull
  public BooleanExpression is(@Nonnull final T enumValue) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(EnumPropertyId.this);
        }

        return checker.parse(EnumPropertyId.this) == enumValue;
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(formatPropertyName(EnumPropertyId.this), true,
            EnumPropertyId.this.getCodec().formatValue(enumValue));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(formatPropertyName(checker, EnumPropertyId.this), eval(checker),
              getCodec().formatValue(checker.parse(EnumPropertyId.this)));
        } catch (MissingPropertyException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  private String format(@Nonnull String left, boolean set, @Nonnull String right) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is ");
    if (!set) {
      sb.append("not ");
    }
    sb.append("set to '");
    sb.append(right);
    sb.append('\'');

    return sb.toString();
  }
}
