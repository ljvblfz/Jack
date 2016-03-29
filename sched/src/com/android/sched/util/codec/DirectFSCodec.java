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


import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.vfs.CachedDirectFS;
import com.android.sched.vfs.VFS;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link VFS} backed by a
 * filesystem directory.
 */
public class DirectFSCodec extends FileOrDirCodec<VFS> {

  public DirectFSCodec() {
    super(Existence.MUST_EXIST, Permission.READ | Permission.WRITE);

    assert (permissions & Permission.EXECUTE) == 0;
  }

  public DirectFSCodec(@Nonnull Existence existence) {
    super(existence, Permission.READ | Permission.WRITE);
  }

  @Nonnull
  public DirectFSCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public DirectFSCodec changeAllPermission() {
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
    return "a path to a directory (" + getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "dir";
  }

  @Override
  @Nonnull
  public VFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    try {
      return new CachedDirectFS(new Directory(context.getWorkingDirectory(), string,
          context.getRunnableHooks(), existence, permissions, change), permissions);
    } catch (IOException e) {
      throw new ParsingException(e);
    }
  }
}
