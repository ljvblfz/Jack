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
import com.android.sched.util.stream.UncloseableOutputStream;
import com.android.sched.util.stream.UncloseablePrintStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a output stream from a file path or a standard output.
 */
public class OutputStreamFile extends AbstractStreamFile {
  private final boolean append;

  public OutputStreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      boolean append)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException {
    super(name, hooks);

    performChecks(existence, Permission.WRITE, change);

    this.append = append;
  }

  /**
   * Creates a new instance of {@link OutputStreamFile} assuming the file may exist or not, without
   * modifying its permissions. If the file already exists it will be overwritten.
   */
  public OutputStreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks)
      throws CannotCreateFileException,
      WrongPermissionException,
      NotFileOrDirectoryException {
    super(name, hooks);

    try {
      performChecks(Existence.MAY_EXIST, Permission.WRITE, ChangePermission.NOCHANGE);
    } catch (NoSuchFileException e) {
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotSetPermissionException e) {
      throw new AssertionError(e);
    }

    this.append = false;
  }

  public OutputStreamFile() {
    super(Permission.WRITE);

    this.append = true;
  }

  @Nonnull
  public OutputStream getOutputStream() {
    if (file == null) {
      return new UncloseableOutputStream(System.out);
    } else {
      clearRemover();
      try {
        return new FileOutputStream(file, append);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }
  }

  @Nonnull
  public PrintStream getPrintStream() {
    if (file == null) {
      return new UncloseablePrintStream(System.out);
    } else {
      clearRemover();
      try {
        return new PrintStream(file);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }
  }
}