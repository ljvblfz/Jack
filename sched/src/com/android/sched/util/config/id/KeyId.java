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

import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.PropertyNotRequiredException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * An instance of this type identifies a specific value.
 *
 * @param <T> Type of the value.
 * @param <S> Raw type for this id.
 */
public abstract class KeyId<T, S> {
  @Nonnull
  private final String name;

  @Nonnull
  private final Map<Class<? extends Category>, Category> categories =
      new HashMap<Class<? extends Category>, Category>(2);

  public KeyId(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public Collection<Class<? extends Category>> getCategories() {
    return Collections.unmodifiableCollection(categories.keySet());
  }

  @Nonnull
  public KeyId<T, S> addCategory(@Nonnull Class<? extends Category> category) {
    if (!hasCategory(category)) {
      this.categories.put(category, null);
    } else {
      throw new ConfigurationError("Duplicate category " + category.getCanonicalName());
    }

    return this;
  }

  @Nonnull
  public KeyId<T, S> addCategory(@Nonnull Category category) {
    if (!hasCategory(category.getClass())) {
      this.categories.put(category.getClass(), category);
    } else {
      throw new ConfigurationError("Duplicate category " + category.getClass().getCanonicalName());
    }

    return this;
  }

  public boolean hasDirectCategory(@Nonnull Class<? extends Category> target) {
    if (target == Category.class || categories.containsKey(target)) {
      return true;
    }

    return false;
  }

  @CheckForNull
  public Category getDirectCategory(@Nonnull Class<? extends Category> target) {
    if (target == Category.class) {
      return null;
    }

    if (categories.containsKey(target)) {
      return categories.get(target);
    }

    throw new NoSuchElementException();
  }

  public boolean hasCategory(@Nonnull Class<? extends Category> target) {
    if (target == Category.class) {
      return true;
    }

    for (Class<? extends Category> category : categories.keySet()) {
      if (target.isAssignableFrom(category)) {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @CheckForNull
  public <T extends Category> T getCategory(@Nonnull Class<T> target) {
    if (target == Category.class) {
      return null;
    }

    for (Class<? extends Category> category : categories.keySet()) {
      if (target.isAssignableFrom(category)) {
        return (T) categories.get(target);
      }
    }

    throw new NoSuchElementException();
  }


  @CheckForNull
  private BooleanExpression requiredIf;

  @Nonnull
  public KeyId<T, S> requiredIf(@Nonnull BooleanExpression expression) {
    this.requiredIf = expression;

    return this;
  }

  @CheckForNull
  public BooleanExpression getRequiredExpression() {
    return requiredIf;
  }

  public boolean isRequired(@Nonnull ConfigChecker checker) throws PropertyIdException {
    if (requiredIf == null) {
      return true;
    }

    try {
      return requiredIf.eval(checker);
    } catch (PropertyNotRequiredException e) {
      return false;
    }
  }

  @Override
  @Nonnull
  public String toString() {
    return name;
  }
}
