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
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.ReaderFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.StandardInputLocation;

import java.nio.charset.Charset;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link ReaderFileCodec} is used to create an instance of {@link ReaderFile}.
 */
public class ReaderFileCodec extends FileCodec<ReaderFile> {
  @Nonnegative
  private int bufferSize = 8 * 1024;

  public ReaderFileCodec() {
    super(Existence.MUST_EXIST, Permission.READ);
  }

  @Nonnull
  public ReaderFileCodec allowStandardInput() {
    this.allowStandardIO = true;

    return this;
  }

  @Nonnull
  public ReaderFileCodec allowCharset() {
    this.charsetCodec = new CharsetCodec().withMinCharsetToDisplay(5);

    return this;
  }

  @Nonnull
  public ReaderFileCodec allowCharset(@Nonnull CharsetCodec codec) {
    this.charsetCodec = codec;

    return this;
  }

  @Nonnull
  public ReaderFileCodec withDefaultCharset(@Nonnull Charset defaultCharset) {
    this.defaultCharset = defaultCharset;

    return this;
  }

  @Nonnull
  public ReaderFileCodec withBuffer(@Nonnegative int bufferSize) {
    this.bufferSize = bufferSize;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull ReaderFile reader) {
    String string;

    if (reader.isStandard()) {
      string = STANDARD_IO_NAME;
    } else {
      string = reader.getPath();
    }

    if (charsetCodec != null) {
      string = string + "[" + reader.getCharset().displayName() + "]";
    }

    return string;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull ReaderFile reader)
      throws CheckingException {
    if (reader.isStandard() && !allowStandardIO) {
      throw new CheckingException("Standard input is not allowed");
    }
  }

  @Override
  @Nonnull
  public ReaderFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Nonnull
  private static final Location STANDARD_INPUT_LOCATION = new StandardInputLocation();

  @Override
  @Nonnull
  public ReaderFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    Charset localCharset = parseCharset(context, string);
    if (localCharset != null) {
      string = string.substring(0, string.lastIndexOf('['));
    }

    if (string.equals(STANDARD_IO_NAME)) {
      if (!allowStandardIO) {
        throw new ParsingException("Standard input can not be used");
      }

      return new ReaderFile(context.getStandardInput(),
          getCharset(context, localCharset), bufferSize, STANDARD_INPUT_LOCATION);
    } else {
      try {
        return new ReaderFile(context.getWorkingDirectory(), string,
            getCharset(context, localCharset), bufferSize);
      } catch (NoSuchFileException | NotFileException | WrongPermissionException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }
  }
}
