/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import com.android.sched.util.config.ConfigurationError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} tries successively several {@link StringCodec}s until one is successful.
 *
 * @param <T> Element type
 */
public class OrCodec<T> implements StringCodec<T> {
  @Nonnull
  protected final List<StringCodec<? extends T>> codecList;

  public OrCodec(@Nonnull List<StringCodec<? extends T>> codecList) {
    assert codecList.size() >= 2;
    this.codecList = codecList;
  }

  public OrCodec(@Nonnull StringCodec<? extends T>... codecList) {
    assert codecList.length >= 2;
    this.codecList = Arrays.asList(codecList);
  }

  @Override
  @Nonnull
  public T parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public T checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException  {
    for (StringCodec<? extends T> codec : codecList) {
      try {
        return codec.checkString(context, string);
      } catch (ParsingException e) {
        // try next codec
      }
    }
    return codecList.get(0).checkString(context, string);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull T data) {
  }

  @Override
  @Nonnull
  public String getUsage() {
    return Joiner.on(" or ").join(
        Collections2.transform(codecList, new Function<StringCodec<? extends T>, String>() {
          @Override
          public String apply(@Nonnull StringCodec<? extends T> codec) {
            return codec.getUsage();
          }
        }));
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return Joiner.on("-or-").join(
        Collections2.transform(codecList, new Function<StringCodec<? extends T>, String>() {
          @Override
          public String apply(@Nonnull StringCodec<? extends T> codec) {
            return codec.getVariableName();
          }
        }));
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public String formatValue(@Nonnull T data) {
    for (StringCodec<? extends T> codec : codecList) {
      try {
        return ((StringCodec<T>) codec).formatValue(data);
      } catch (ClassCastException e) {
        // try next codec
      }
    }
    throw new AssertionError();
  }
}