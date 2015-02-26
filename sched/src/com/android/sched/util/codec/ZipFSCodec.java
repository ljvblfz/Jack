/*
 * Copyright (C) 2015 The Android Open Source Project
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


import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.vfs.ReadWriteZipFS;
import com.android.sched.vfs.VFS;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link VFS} backed by a
 * filesystem directory, which is then zipped when closed.
 */
public class ZipFSCodec extends FileOrDirCodec<VFS> {

  public ZipFSCodec(@Nonnull Existence existence) {
    super(existence, Permission.READ | Permission.WRITE);
  }

  @Nonnull
  public ZipFSCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public ZipFSCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull VFS dir) {
  }

  @Override
  @Nonnull
  public VFS parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull VFS directory) {
    return directory.getPath();
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a zip archive (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public VFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      return new ReadWriteZipFS(new OutputZipFile(string, hooks, existence, change));
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
