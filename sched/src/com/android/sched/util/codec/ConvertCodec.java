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

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to convert a {@code StringCodec<SRC>} to a
 * {@code StringCodec<DST>}.
 */
public abstract class ConvertCodec<SRC, DST> implements StringCodec<DST> {
  @Nonnull
  protected final StringCodec<SRC> codec;

  public ConvertCodec (@Nonnull StringCodec<SRC> codec) {
    this.codec = codec;
  }

  @Nonnull
  protected abstract DST convert(@Nonnull SRC src) throws ParsingException;

  @Nonnull
  protected abstract SRC revert(@Nonnull DST dst);

  @Override
  @Nonnull
  public DST parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return convert(codec.parseString(context, string));
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public DST checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    SRC src = codec.checkString(context, string);

    if (src == null) {
      return null;
    } else {
      return convert(src);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull DST data) {
    return codec.formatValue(revert(data));
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull DST data)
      throws CheckingException {
    codec.checkValue(context, revert(data));
  }

  @Override
  @Nonnull
  public String getUsage() {
    return codec.getUsage();
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return codec.getVariableName();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return codec.getValueDescriptions();
  }
}