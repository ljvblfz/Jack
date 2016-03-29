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

import com.android.sched.util.LineSeparator;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.OutputStreamFile.StandardOutputKind;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.StandardErrorLocation;
import com.android.sched.util.location.StandardOutputLocation;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link WriterFileCodec} is used to create an instance of {@link WriterFile}.
 */
public class WriterFileCodec extends FileCodec<WriterFile> {
  private boolean append = false;
  @Nonnull
  private LineSeparator lineSeparator = LineSeparator.SYSTEM;
  @Nonnegative
  private int bufferSize = 8 * 1024;

  public WriterFileCodec(@Nonnull Existence existence) {
    super(existence, Permission.WRITE);
  }

  @Nonnull
  public WriterFileCodec allowStandardOutputOrError() {
    this.allowStandardIO = true;
    this.allowStandardError = true;

    return this;
  }

  @Nonnull
  public WriterFileCodec allowStandardOutput() {
    this.allowStandardIO = true;

    return this;
  }

  @Nonnull
  public WriterFileCodec allowStandardError() {
    this.allowStandardError = true;

    return this;
  }

  @Nonnull
  public WriterFileCodec makeAppendable() {
    this.append = true;

    return this;
  }

  @Nonnull
  public WriterFileCodec withLineSeparator(@Nonnull LineSeparator lineSeparator) {
    this.lineSeparator = lineSeparator;

    return this;
  }

  @Nonnull
  public WriterFileCodec withBuffer(@Nonnegative int bufferSize) {
    this.bufferSize = bufferSize;

    return this;
  }

  @Nonnull
  public WriterFileCodec withoutBuffer() {
    this.bufferSize = 0;

    return this;
  }

  @Nonnull
  public WriterFileCodec allowCharset() {
    this.charsetCodec = new CharsetCodec().withMinCharsetToDisplay(5).forEncoder();

    return this;
  }

  @Nonnull
  public WriterFileCodec allowCharset(@Nonnull CharsetCodec codec) {
    this.charsetCodec = codec;

    return this;
  }

  @Nonnull
  public WriterFileCodec withDefaultCharset(@Nonnull Charset defaultCharset) {
    this.defaultCharset = defaultCharset;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull WriterFile writer) {
    String string;

    if (writer.isStandard()) {
      if (writer.getLocation().equals(StandardOutputKind.STANDARD_OUTPUT.getLocation())) {
        string = STANDARD_IO_NAME;
      } else {
        assert writer.getLocation().equals(StandardOutputKind.STANDARD_ERROR.getLocation());

        string = STANDARD_ERROR_NAME;
      }
    } else {
      string = writer.getPath();
    }

    if (charsetCodec != null) {
      string = string + "[" + writer.getCharset().displayName() + "]";
    }

    return string;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull WriterFile writer)
      throws CheckingException {
    Location location = writer.getLocation();

    if (location.equals(StandardOutputKind.STANDARD_OUTPUT.getLocation()) && !allowStandardIO) {
      throw new CheckingException("Standard output is not allowed");
    } else if (location.equals(StandardOutputKind.STANDARD_ERROR.getLocation())
        && !allowStandardError) {
      throw new CheckingException("Standard error is not allowed");
    }
  }

  @Override
  @Nonnull
  public WriterFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Nonnull
  private static final Location STANDARD_OUTPUT_LOCATION = new StandardOutputLocation();
  @Nonnull
  private static final Location STANDARD_ERROR_LOCATION = new StandardErrorLocation();

  @Override
  @Nonnull
  public WriterFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    Charset localCharset = parseCharset(context, string);
    if (localCharset != null) {
      string = string.substring(0, string.lastIndexOf('['));
    }

    if (string.equals(STANDARD_IO_NAME)) {
      if (!allowStandardIO) {
        throw new ParsingException("Standard input can not be used");
      }

      return new WriterFile(context.getStandardOutput(),
          getCharset(context, localCharset), lineSeparator, bufferSize,
          STANDARD_OUTPUT_LOCATION);
    } else if (string.equals(STANDARD_ERROR_NAME)) {
      if (!allowStandardError) {
        throw new ParsingException("Standard error can not be used");
      }

      return new WriterFile(context.getStandardError(),
          getCharset(context, localCharset), lineSeparator, bufferSize,
          STANDARD_ERROR_LOCATION);
    } else {
      try {
        return new WriterFile(context.getWorkingDirectory(),
            string,
            getCharset(context, localCharset),
            lineSeparator,
            bufferSize,
            context.getRunnableHooks(),
            existence,
            change,
            append);
      } catch (IOException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }
  }
}
