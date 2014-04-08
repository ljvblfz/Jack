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
import com.android.sched.util.file.StreamFile;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link OutputStreamCodec} is used to create an instance of {@link OutputStreamFile}.
 */
public class OutputStreamCodec extends StreamCodec
    implements StringCodec<OutputStreamFile> {
  private boolean append        = false;

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
  public OutputStreamCodec allowStandard() {
    this.allowStandard = true;

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
    return formatValue((StreamFile) stream);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull OutputStreamFile stream)
      throws CheckingException {
    checkValue(context, (StreamFile) stream);
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
    super.checkString(context, string);

    if (string.equals(STANDARD_IO_NAME)) {
      return new OutputStreamFile();
    } else {
      try {
        return new OutputStreamFile(string, context.getRunnableHooks(), existence, change, append);
      } catch (IOException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }
  }
}
