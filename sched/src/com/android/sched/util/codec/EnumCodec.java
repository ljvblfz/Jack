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

import com.android.sched.util.codec.KeyValueCodec.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to return element of an enum based on its name
 *
 * @param <T> The enum type
 */
public class EnumCodec<T extends Enum<T>> implements StringCodec<T> {
  @Nonnull
  KeyValueCodec<T> parser;

  public EnumCodec(@Nonnull T[] values) {
    @SuppressWarnings("unchecked")
    Entry<T>[] entries = new Entry[values.length];

    int idx = 0;
    for (T value : values) {
      entries[idx++] = new Entry<T>(value.name().replace('_', '-'), value);
    }

    parser = new KeyValueCodec<T>(entries);
  }

  @Nonnull
  public EnumCodec<T> ignoreCase() {
    parser.ignoreCase();

    return this;
  }

  @Nonnull
  public EnumCodec<T> sorted() {
    parser.sorted();

    return this;
  }

  @Override
  @Nonnull
  public T parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return parser.parseString(context, string);
  }

  @Override
  @CheckForNull
  public T checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    return parser.checkString(context, string);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull T value)
      throws CheckingException {
    parser.checkValue(context, value);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return parser.getUsage();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull T value) {
    return parser.formatValue(value);
  }
}
