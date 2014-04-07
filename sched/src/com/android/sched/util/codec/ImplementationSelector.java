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

package com.android.sched.util.codec;

import com.google.common.base.Joiner;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.ReflectDefaultCtorFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of any type with a
 * default constructor for which an implementation has been assigned with
 * {@link ImplementationName}.
 *
 * @param <T>
 */
public class ImplementationSelector<T> extends Selector<T> implements StringCodec<T> {
  private boolean bypassAccessibility = false;

  public ImplementationSelector(@Nonnull Class<T> type) {
    super(type);
  }

  public ImplementationSelector<T> bypassAccessibility() {
    this.bypassAccessibility = true;

    return this;
  }

  @Override
  @CheckForNull
  public T checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    getClass(string);

    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull T data)
      throws CheckingException {
    if (!checkClass((Class<? extends T>) data.getClass())) {
      throw new CheckingException("The value must be an instance of {"
          + Joiner.on(',').join(getClasses()) + "} but is an instance of '"
          + data.getClass().getCanonicalName() + "'");
    }
  }

  @Override
  @Nonnull
  public T parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return new ReflectDefaultCtorFactory<T>(getClass(string), !bypassAccessibility).create();
    } catch (ParsingException e) {
      throw new ConfigurationError(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public String formatValue(@Nonnull T data) {
    return getName((Class<? extends T>) data.getClass());
  }
}
