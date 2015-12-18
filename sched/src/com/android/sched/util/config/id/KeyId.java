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
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.PropertyNotRequiredException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
  private final Set<Class<? extends Category>> categories =
      new HashSet<Class<? extends Category>>(2);

  public KeyId(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public Collection<Class<? extends Category>> getCategories() {
    return Collections.unmodifiableCollection(categories);
  }

  @Nonnull
  public KeyId<T, S> addCategory(@Nonnull Class<? extends Category> category) {
    this.categories.add(category);

    return this;
  }

  public boolean hasDirectCategory(@Nonnull Class<? extends Category> target) {
    if (target == Category.class || categories.contains(target)) {
      return true;
    }

    return false;
  }

  public boolean hasCategory(@Nonnull Class<? extends Category> target) {
    if (target == Category.class) {
      return true;
    }

    for (Class<? extends Category> category : categories) {
      if (category.isAssignableFrom(target)) {
        return true;
      }
    }

    return false;
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
