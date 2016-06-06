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


import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.VFS;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputVFS}
 * to directories.
 */
public class DirectoryInputVFSCodec extends InputVFSCodec
    implements StringCodec<InputVFS> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

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
  public InputVFS checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    try {
      VFS vfs = new DirectFS(new Directory(context.getWorkingDirectory(),
          string,
          context.getRunnableHooks(),
          Existence.MUST_EXIST,
          Permission.READ,
          change), Permission.READ);
      return new GenericInputVFS(vfs);
    } catch (CannotChangePermissionException | NotDirectoryException | WrongPermissionException
        | NoSuchFileException | FileAlreadyExistsException | CannotCreateFileException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
