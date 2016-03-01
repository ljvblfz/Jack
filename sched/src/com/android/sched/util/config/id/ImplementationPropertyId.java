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

import com.android.sched.util.codec.ImplementationSelector;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.ClassExpression;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages implementation properties of type {@code T}
 *
 * @param <T> the type of the implementation properties
 */
public class ImplementationPropertyId<T> extends PropertyId<T> {
  @Nonnull
  public static <T> ImplementationPropertyId<T> create(@Nonnull String name,
      @Nonnull String description, @Nonnull Class<T> type) {
    return new ImplementationPropertyId<T>(name, description, new ImplementationSelector<T>(type));
  }

  protected ImplementationPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull ImplementationSelector<T> parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public ImplementationPropertyId<T> addDefaultValue(@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public ImplementationPropertyId<T> addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public ImplementationPropertyId<T> addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }

  @Nonnull
  public ImplementationPropertyId<T> bypassAccessibility() {
    getCodec().bypassAccessibility();

    return this;
  }

  @Override
  @Nonnull
  public ImplementationPropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public ImplementationSelector<T> getCodec() {
    return (ImplementationSelector<T>) super.getCodec();
  }

  @Nonnull
  public ClassExpression<T> getClazz() {
    return new ClassExpression<T>(getCodec()) {
      @Override
      public Class<? extends T> eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(ImplementationPropertyId.this);
        }

        try {
          return ImplementationPropertyId.this.getCodec()
              .getClass(checker.getRawValue(ImplementationPropertyId.this));
        } catch (ParsingException e) {
          throw new PropertyIdException(
              ImplementationPropertyId.this, checker.getLocation(ImplementationPropertyId.this), e);
        }
      }

      @Override
      @Nonnull
      public String getDescription() {
        return formatPropertyName(ImplementationPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return formatPropertyName(checker, ImplementationPropertyId.this);
      }
    };
  }
}
