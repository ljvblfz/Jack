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
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.DirectDir;
import com.android.sched.vfs.InputOutputVDir;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputOutputVDir}.
 */
public class InputOutputVDirCodec extends FileOrDirCodec
    implements StringCodec<InputOutputVDir> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public InputOutputVDirCodec(@Nonnull Existence existence) {
    super(existence, Permission.READ | Permission.WRITE);
  }

  @Nonnull
  public InputOutputVDirCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public InputOutputVDirCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("a path to a directory (must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)     != 0, "be readable");
    sb.append((permissions & Permission.WRITE)    != 0, "be writable");

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public InputOutputVDir checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    try {
      return new DirectDir(
          new Directory(string, context.getRunnableHooks(), existence, permissions, change));
    } catch (IOException e) {
      throw new ParsingException(e);
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputOutputVDir dir) {
  }

  @Override
  @Nonnull
  public InputOutputVDir parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputOutputVDir directory) {
    return directory.getLocation().getDescription();
  }
}
