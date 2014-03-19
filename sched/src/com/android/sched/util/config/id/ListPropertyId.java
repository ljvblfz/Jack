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


import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.LongExpression;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link List}
 *
 * @param <T> the type of elements in this list
 */
public class ListPropertyId<T> extends PropertyId<List<T>> {
  @Nonnull
  public static <T> ListPropertyId<T> create(@Nonnull String name, @Nonnull String description,
      @Nonnull String var, @Nonnull StringCodec<T> parser) {
    return new ListPropertyId<T>(name, description, new ListCodec<T>(var, parser));
  }

  protected ListPropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull ListCodec<T> parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public ListPropertyId<T> addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public ListPropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public ListCodec<T> getCodec() {
    return (ListCodec<T>) super.getCodec();
  }

  @Nonnull
  public ListPropertyId<T> on(@Nonnull String separator) {
    getCodec().setSeperator(separator);

    return this;
  }

  @Nonnull
  public ListPropertyId<T> minElements(@Nonnegative int min) {
    getCodec().setMin(min);

    return this;
  }

  @Nonnull
  public ListPropertyId<T> maxElements(@Nonnegative int max) {
    getCodec().setMax(max);

    return this;
  }

  @Nonnull
  public BooleanExpression isEmpty() {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(ListPropertyId.this);
        }

        return checker.parse(ListPropertyId.this).isEmpty();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(formatPropertyName(ListPropertyId.this), true);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(formatPropertyName(checker, ListPropertyId.this), eval(checker));
        } catch (MissingPropertyException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isNotEmpty() {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(ListPropertyId.this);
        }

        return !checker.parse(ListPropertyId.this).isEmpty();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(formatPropertyName(ListPropertyId.this), false);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(formatPropertyName(checker, ListPropertyId.this), !eval(checker));
        } catch (MissingPropertyException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public LongExpression getSize() {
    return new LongExpression() {
      @Override
      public long eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(ListPropertyId.this);
        }

        return checker.parse(ListPropertyId.this).size();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return "number of elements of " + formatPropertyName(ListPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return "number of elements of " + formatPropertyName(checker, ListPropertyId.this);
      }
    };
  }

  @Nonnull
  private String format(@Nonnull String str, boolean empty) {
    StringBuilder sb = new StringBuilder();

    sb.append(str);
    sb.append(" is ");
    if (!empty) {
      sb.append("not ");
    }
    sb.append("empty");

    return sb.toString();
  }
}
