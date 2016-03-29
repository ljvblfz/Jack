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
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.location.StringLocation;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link FileOrDirectory}.
 */
public class InputFileOrDirectoryCodec extends FileOrDirCodec<FileOrDirectory> {

  public InputFileOrDirectoryCodec() {
    super(Existence.MUST_EXIST, Permission.READ);
  }

  @Override
  @Nonnull
  public FileOrDirectory parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public FileOrDirectory checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      Directory workingDir = context.getWorkingDirectory();
      File file = FileOrDirectory.getFileFromWorkingDirectory(workingDir, string);
      if (!file.exists()) {
        throw new ParsingException(new NoSuchFileException(new StringLocation(string)));
      } else if (file.isFile()) {
        return new InputStreamFile(workingDir, string);
      } else {
        return new Directory(workingDir,
            string,
            context.getRunnableHooks(),
            existence,
            permissions,
            change);
      }
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }

  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a file or directory (" + getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "file-or-dir";
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull FileOrDirectory data) {
    return data.getPath();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull FileOrDirectory data) {
  }
}
