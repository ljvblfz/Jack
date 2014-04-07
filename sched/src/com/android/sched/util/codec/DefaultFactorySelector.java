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
import com.android.sched.util.config.DefaultFactory;
import com.android.sched.util.config.ReflectDefaultCtorFactory;

import javax.annotation.Nonnull;

/**
 * This factory is used to create an instance of a {@link DefaultFactory} of any type
 * with a default constructor for which an implementation has been assigned with
 * {@link ImplementationName}.
 *
 * @param <T> the base type of all implementations
 */
public class DefaultFactorySelector<T> extends Selector<T>
    implements StringCodec<DefaultFactory<T>> {
  private boolean bypassAccessibility = false;

  public DefaultFactorySelector(@Nonnull Class<T> type) {
    super(type);
  }

  @Nonnull
  public DefaultFactorySelector<T> bypassAccessibility() {
    this.bypassAccessibility = true;

    return this;
  }

  @Override
  @Nonnull
  public DefaultFactory<T> checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    return new ReflectDefaultCtorFactory<T>(getClass(string), !bypassAccessibility);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull DefaultFactory<T> factory)
      throws CheckingException {
    if (!checkClass(factory.getInstanciatedClass())) {
      throw new CheckingException("The value must be a DefaultFactory<{"
          + Joiner.on(',').join(getClasses()) + "}> but is a DefaultFactory<"
          + factory.getInstanciatedClass().getCanonicalName() + ">");
    }
  }

  @Override
  @Nonnull
  public DefaultFactory<T> parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull DefaultFactory<T> factory) {
    return getName(factory.getInstanciatedClass());
  }
}
