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
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.OutputStreamFile.StandardOutputKind;
import com.android.sched.util.location.Location;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link OutputStreamCodec} is used to create an instance of {@link OutputStreamFile}.
 */
public class OutputStreamCodec extends FileCodec<OutputStreamFile> {
  private boolean append = false;

  public OutputStreamCodec(@Nonnull Existence existence) {
    super(existence, Permission.WRITE);
  }

  @Nonnull
  public OutputStreamCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public OutputStreamCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Nonnull
  public OutputStreamCodec allowStandardOutputOrError() {
    this.allowStandardIO = true;
    this.allowStandardError = true;

    return this;
  }

  @Nonnull
  public OutputStreamCodec allowStandardOutput() {
    this.allowStandardIO = true;

    return this;
  }

  @Nonnull
  public OutputStreamCodec allowStandardError() {
    this.allowStandardError = true;

    return this;
  }

  @Nonnull
  public OutputStreamCodec makeAppendable() {
    this.append = true;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull OutputStreamFile stream) {
    if (stream.isStandard()) {
      if (stream.getLocation().equals(StandardOutputKind.STANDARD_OUTPUT.getLocation())) {
        return STANDARD_IO_NAME;
      } else {
        assert stream.getLocation().equals(StandardOutputKind.STANDARD_ERROR.getLocation());

        return STANDARD_ERROR_NAME;
      }
    } else {
      return stream.getPath();
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull OutputStreamFile stream)
      throws CheckingException {
    Location location = stream.getLocation();

    if (location.equals(StandardOutputKind.STANDARD_OUTPUT.getLocation()) && !allowStandardIO) {
      throw new CheckingException("Standard output is not allowed");
    } else if (location.equals(StandardOutputKind.STANDARD_ERROR.getLocation())
        && !allowStandardError) {
      throw new CheckingException("Standard error is not allowed");
    }
  }

  @Override
  @Nonnull
  public OutputStreamFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public OutputStreamFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (string.equals(STANDARD_IO_NAME)) {
      if (!allowStandardIO) {
        throw new ParsingException("Standard output can not be used");
      }

      return new OutputStreamFile(context.getStandardOutput(),
          StandardOutputKind.STANDARD_OUTPUT.getLocation());
    } else if (string.equals(STANDARD_ERROR_NAME)) {
      if (!allowStandardError) {
        throw new ParsingException("Standard error can not be used");
      }

      return new OutputStreamFile(context.getStandardError(),
          StandardOutputKind.STANDARD_ERROR.getLocation());
    } else {
      try {
        return new OutputStreamFile(context.getWorkingDirectory(),
            string,
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
