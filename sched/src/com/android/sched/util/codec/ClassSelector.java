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



import com.android.sched.util.config.ConfigurationError;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec}  is used to create an instance of Class for which an
 * implementation has been assigned with {@link ImplementationName}.
 *
 * @param <T> the base type of all implementations
 */
public class ClassSelector<T> extends Selector<T> implements StringCodec<Class<? extends T>> {
  public ClassSelector(@Nonnull Class<T> type) {
    super(type);
  }

  @Override
  @Nonnull
  public Class<? extends T> checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    return getClass(string);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Class<? extends T> cls)
      throws CheckingException {
    if (!checkClass(cls)) {
      throw new CheckingException(
          "The value must be " + getUsage() + " but is '" + formatValue(cls) + "'");
    }
  }

  @Override
  @Nonnull
  public Class<? extends T> parseString(@Nonnull CodecContext context,  @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Class<? extends T> type) {
    return getName(type);
  }
}
