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
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.OutputZipFile.Compression;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.vfs.ReadWriteZipFS;
import com.android.sched.vfs.VFS;

import java.security.Provider.Service;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link VFS} backed by a
 * filesystem directory, which is then zipped when closed.
 */
public class ZipFSCodec extends FileOrDirCodec<VFS> {

  @Nonnull
  private final MessageDigestCodec messageDigestCodec = new MessageDigestCodec();
  @Nonnull
  private final Compression compression;

  public ZipFSCodec(@Nonnull Existence existence, @Nonnull Compression compression) {
    super(existence, Permission.READ | Permission.WRITE);
    this.compression = compression;
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
    return "a path to a zip archive (" + getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "zip";
  }

  @Override
  @Nonnull
  public VFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      Service service = messageDigestCodec.checkString(context, "SHA");
      return new ReadWriteZipFS(
          new OutputZipFile(context.getWorkingDirectory(), string, hooks, existence, change,
              compression),
          /* numGroups = */ 1, /* groupSize = */ 2,
          new MessageDigestFactory(service), /* debug = */ false);
    } catch (CannotCreateFileException | NotDirectoryException | NotFileException
        | WrongPermissionException | CannotChangePermissionException | NoSuchFileException
        | FileAlreadyExistsException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
