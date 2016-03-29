/*
 * Copyright (C) 2016 The Android Open Source Project
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link Charset}
 */
public class CharsetCodec implements StringCodec<Charset> {
  private boolean forEncoder = false;
  @Nonnegative
  private int minCharsetToDisplay = 10;
  @Nonnegative
  private int maxCharsetToDisplay = Integer.MAX_VALUE;

  @Override
  @Nonnull
  public Charset parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return Charset.forName(string);
    } catch (Exception e) {
      throw new ConfigurationError(e.getMessage(), e);
    }
  }

  @Override
  @CheckForNull
  public Charset checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (!Charset.isSupported(string)) {
      throw new ParsingException(
          "The value must be " + getDetailedUsage() + " but is '" + string + "'");
    }

    return null;
  }

  @Nonnull
  public CharsetCodec withMinCharsetToDisplay(@Nonnegative int min) {
    this.minCharsetToDisplay = min;

    return this;
  }

  @Nonnull
  public CharsetCodec withMaxCharsetToDisplay(@Nonnegative int max) {
    this.maxCharsetToDisplay = max;

    return this;
  }

  public CharsetCodec forEncoder() {
    this.forEncoder = true;

    return this;
  }

  @Nonnull
  public String getDetailedUsage() {
    return getUsage(maxCharsetToDisplay);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return getUsage(minCharsetToDisplay);
  }

  @Nonnull
  private String getUsage(@Nonnegative int max) {
    StringBuilder sb = new StringBuilder();
    int count = 0;

    boolean first = true;
    sb.append('{');
    for (Entry<String, Charset> entry : Charset.availableCharsets().entrySet()) {
      if (!forEncoder || entry.getValue().canEncode()) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }

        if (++count > max) {
          sb.append("<...>");
          break;
        } else {
          sb.append(entry.getValue().displayName());
        }
      }
    }
    sb.append("} (case insensitive)");

    return sb.toString();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    List<ValueDescription> list = new ArrayList<ValueDescription>();

    for (Entry<String, Charset> entry : Charset.availableCharsets().entrySet()) {
      if (!forEncoder || entry.getValue().canEncode()) {
        for (String alias : entry.getValue().aliases()) {
          list.add(new ValueDescription(alias, "alias for " + entry.getValue().displayName()));
        }
      }
    }

    return list;
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "charset";
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Charset charset) {
    return charset.name();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Charset charset) {
  }
}