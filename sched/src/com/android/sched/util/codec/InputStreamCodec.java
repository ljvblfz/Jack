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
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.StreamFile;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link InputStreamCodec} is used to create an instance of {@link InputStreamFile}.
 */
public class InputStreamCodec extends StreamCodec
    implements StringCodec<InputStreamFile> {
  public InputStreamCodec(@Nonnull Existence existence) {
    super(existence, Permission.READ);
  }

  @Nonnull
  public InputStreamCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public InputStreamCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Nonnull
  public InputStreamCodec allowStandard() {
    this.allowStandard = true;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputStreamFile stream) {
    return formatValue((StreamFile) stream);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputStreamFile stream)
      throws CheckingException {
    checkValue(context, (StreamFile) stream);
  }

  @Override
  @Nonnull
  public InputStreamFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public InputStreamFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    super.checkString(context, string);

    if (string.equals(STANDARD_IO_NAME)) {
      return new InputStreamFile();
    } else {
      try {
        return new InputStreamFile(string, context.getRunnableHooks(), existence, change);
      } catch (IOException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }
  }
}
