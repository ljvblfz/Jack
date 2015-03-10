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

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} performing no check nor conversion, just providing a usage.
 */
public class StringValueCodec implements StringCodec<String> {
  @Nonnull
  private final String usage;

  public StringValueCodec(@Nonnull String usage) {
    this.usage = usage;
  }

  @Override
  @Nonnull
  public String parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return string;
  }

  @Override
  @CheckForNull
  public String checkString(@Nonnull CodecContext context, @Nonnull String string) {
    return string;
  }

  @Override
  @Nonnull
  public String getUsage() {
    return usage;
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull String data) {
    return data;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull String data) {
  }
}