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


import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputVFS}.
 */
public class InputVFSCodec extends FileOrDirCodec<InputVFS> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public InputVFSCodec() {
    super(Existence.MUST_EXIST, Permission.READ);

    assert (permissions & Permission.EXECUTE) == 0;
  }

  @Nonnull
  public InputVFSCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public InputVFSCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a directory or zip archive (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "zip-or-dir";
  }

  @Override
  @Nonnull
  public InputVFS checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    final VFS vfs;
    try {
      File dirOrZip = new File(string);
      if (dirOrZip.isDirectory()) {
        vfs = new DirectFS(new Directory(context.getWorkingDirectory(),
            string,
            context.getRunnableHooks(),
            Existence.MUST_EXIST,
            Permission.READ,
            change), Permission.READ);
      } else {
        RunnableHooks hooks = context.getRunnableHooks();
        assert hooks != null;
        vfs = new ReadZipFS(new InputZipFile(context.getWorkingDirectory(), string, hooks,
            Existence.MUST_EXIST, change));
      }

      return new GenericInputVFS(vfs);
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputVFS dir) {
  }

  @Override
  @Nonnull
  public InputVFS parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputVFS directory) {
    return directory.getPath();
  }
}
