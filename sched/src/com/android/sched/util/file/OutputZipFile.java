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

package com.android.sched.util.file;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.RunnableHooks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a zip file designed to be written to.
 */
public class OutputZipFile extends OutputStreamFile {

  public OutputZipFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException {
    super(name, hooks, existence, change, false);
  }

  @Override
  @Nonnull
  public ZipOutputStream getOutputStream() {
    assert file != null;
    clearRemover();
    try {
      return new ZipOutputStream(new FileOutputStream(file));
    } catch (FileNotFoundException e) {
      throw new ConcurrentIOException(e);
    }
  }

  @Override
  @Nonnull
  public PrintStream getPrintStream() {
    return new PrintStream(getOutputStream());
  }

  @Nonnull
  public String getName() {
    assert file != null;
    return file.getName();
  }
}