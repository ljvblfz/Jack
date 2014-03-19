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

package com.android.sched.util.codec;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.ReflectFactory;

import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * This factory is used to create an instance of a {@link ReflectFactory} of any type with a
 * constructor with parameters for which an implementation has been assigned with
 * {@link ImplementationName}.
 *
 * @param <T> the base type of all implementations
 */
public class ReflectFactorySelector<T>
    extends Selector<T>
    implements StringCodec<ReflectFactory<T>> {
  private boolean bypassAccessibility = false;
  @Nonnull
  private final ArrayList<Class<?>> argTypes = new ArrayList<Class<?>>(1);

  public ReflectFactorySelector(@Nonnull Class<T> cls) {
    super(cls);
  }

  @Nonnull
  public ReflectFactorySelector<T> bypassAccessibility() {
    this.bypassAccessibility = true;

    return this;
  }

  @Nonnull
  public ReflectFactorySelector<T> addArgType(@Nonnull Class<?> argType) {
    this.argTypes.add(argType);

    return this;
  }

  @Override
  @Nonnull
  public ReflectFactory<T> parseString(
      @Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public ReflectFactory<T> checkString(
      @Nonnull CodecContext context, @Nonnull String string) throws ParsingException {
    Class<?>[] types = argTypes.toArray(new Class<?>[argTypes.size()]);
    return new ReflectFactory<T>(getClass(string), !bypassAccessibility, types);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull ReflectFactory<T> factory)
      throws CheckingException {
    if (!checkClass(factory.getInstanciatedClass())) {
      throw new CheckingException(
          "The value must be " + getUsage() + " but is '" + formatValue(factory) + "'");
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull ReflectFactory<T> factory) {
    return getName(factory.getInstanciatedClass());
  }
}
