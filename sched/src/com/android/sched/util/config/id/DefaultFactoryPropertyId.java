/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.util.codec.DefaultFactorySelector;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.DefaultFactory;
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
public class DefaultFactoryPropertyId<T> extends PropertyId<DefaultFactory<T>> {
  @Nonnull
  public static <T> DefaultFactoryPropertyId<T> create(@Nonnull String name,
      @Nonnull String description, @Nonnull Class<T> type) {
    return new DefaultFactoryPropertyId<T>(name, description, new DefaultFactorySelector<T>(type));
  }

  protected DefaultFactoryPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull DefaultFactorySelector<T> parser) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public DefaultFactoryPropertyId<T> addDefaultValue(@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Nonnull
  public DefaultFactoryPropertyId<T> bypassAccessibility() {
    getCodec().bypassAccessibility();

    return this;
  }

  @Override
  @Nonnull
  public DefaultFactoryPropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public DefaultFactorySelector<T> getCodec() {
    return (DefaultFactorySelector<T>) super.getCodec();
  }

  @Nonnull
  public ClassExpression<T> getClazz() {
    return new ClassExpression<T>(getCodec()) {
      @Override
      public Class<? extends T> eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(DefaultFactoryPropertyId.this);
        }

        try {
          return DefaultFactoryPropertyId.this.getCodec()
              .getClass(checker.getRawValue(DefaultFactoryPropertyId.this));
        } catch (ParsingException e) {
          throw new PropertyIdException(
              DefaultFactoryPropertyId.this, checker.getLocation(DefaultFactoryPropertyId.this), e);
        }
      }

      @Override
      @Nonnull
      public String getDescription() {
        return formatPropertyName(DefaultFactoryPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return formatPropertyName(checker, DefaultFactoryPropertyId.this);
      }
    };
  }

  @Override
  @Nonnull
  public DefaultFactoryPropertyId<T> addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public DefaultFactoryPropertyId<T> addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }
}
