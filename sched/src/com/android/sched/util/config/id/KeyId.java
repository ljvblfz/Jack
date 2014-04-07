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
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.PropertyNotRequiredException;

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

  public KeyId(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public abstract boolean isPublic();

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
