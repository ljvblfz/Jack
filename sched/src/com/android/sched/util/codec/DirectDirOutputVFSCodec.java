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

import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.GenericOutputVFS;
import com.android.sched.vfs.OutputVFS;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link OutputVFS} backed by a
 * filesystem directory.
 */
public class DirectDirOutputVFSCodec extends OutputVFSCodec {

  @CheckForNull
  private String infoString;

  public DirectDirOutputVFSCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to an output directory (" + getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "dir";
  }

  @Override
  @Nonnull
  public OutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    try {
      DirectFS dirFS = new DirectFS(new Directory(context.getWorkingDirectory(),
          string,
          context.getRunnableHooks(),
          existence,
          permissions,
          change), permissions);
      dirFS.setInfoString(infoString);
      return new GenericOutputVFS(dirFS);
    } catch (CannotChangePermissionException | NotDirectoryException | WrongPermissionException
        | NoSuchFileException | FileAlreadyExistsException | CannotCreateFileException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Nonnull
  public DirectDirOutputVFSCodec setInfoString(@CheckForNull String infoString) {
    this.infoString = infoString;
    return this;
  }

}
