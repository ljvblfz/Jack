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
 * This {@link StringCodec} is used to return an instance of {@link String} among several
 */
public class KeywordsCodec implements StringCodec<String> {
  @Nonnull
  KeyValueCodec<String> parser;

  public KeywordsCodec(@Nonnull String[] keywords) {
    @SuppressWarnings("unchecked")
    Entry<String>[] entries = new Entry[keywords.length];

    int idx = 0;
    for (String keyword : keywords) {
      entries[idx++] = new Entry<String>(keyword, keyword);
    }

    parser = new KeyValueCodec<String>(entries);
  }

  @Nonnull
  public KeywordsCodec ignoreCase() {
    parser.ignoreCase();

    return this;
  }

  @Nonnull
  public KeywordsCodec sort() {
    parser.sorted();

    return this;
  }

  @Override
  @Nonnull
  public String parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return parser.parseString(context, string);
  }

  @Override
  @CheckForNull
  public String checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    return parser.checkString(context, string);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull String string)
      throws CheckingException {
    parser.checkValue(context, string);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return parser.getUsage();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull String value) {
    return parser.formatValue(value);
  }
}
