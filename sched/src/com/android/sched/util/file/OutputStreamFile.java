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
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.QueryableOutputStream;
import com.android.sched.util.stream.UncloseableOutputStream;
import com.android.sched.vfs.OutputStreamProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a output stream from a file path or a standard output.
 */
public class OutputStreamFile extends AbstractStreamFile implements OutputStreamProvider {
  private final boolean append;

  public OutputStreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      boolean append)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    this(new File(name), new FileLocation(name), hooks, existence, change, append);
  }

  public OutputStreamFile(@CheckForNull Directory workingDirectory,
      @Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      boolean append)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    this(getFileFromWorkingDirectory(workingDirectory, name),
        new FileLocation(name),
        hooks,
        existence,
        change,
        append);
  }

  protected OutputStreamFile(@Nonnull File file,
      @Nonnull FileLocation location,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      boolean append)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    super(file, location, hooks);

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
      NotFileException {
    super(name, hooks);

    try {
      performChecks(Existence.MAY_EXIST, Permission.WRITE, ChangePermission.NOCHANGE);
    } catch (NoSuchFileException e) {
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }

    this.append = false;
  }

  /**
   * Creates a new instance of {@link OutputStreamFile} assuming the file must exist, without
   * modifying its permissions. It will be overwritten.
   */
  public OutputStreamFile(@Nonnull String name)
      throws WrongPermissionException, NotFileException {
    super(name, null);

    try {
      performChecks(Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE);
    } catch (NoSuchFileException e) {
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    }

    this.append = false;
  }

  public OutputStreamFile(@Nonnull StandardOutputKind standardOutputKind) {
    super(standardOutputKind.getLocation());
    this.stream = new QueryableOutputStream(
        new UncloseableOutputStream(standardOutputKind.getOutputStream()));
    this.append = true;
  }

  public OutputStreamFile(@Nonnull OutputStream stream, @Nonnull Location location) {
    super(location);
    this.stream = new QueryableOutputStream(new UncloseableOutputStream(stream));
    this.append = true;
  }

  @Override
  @Nonnull
  public synchronized OutputStream getOutputStream() {
    wasUsed = true;
    if (stream == null) {
      clearRemover();

      try {
        stream =  new QueryableOutputStream(new FileOutputStream(file, append));
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return (OutputStream) stream;
  }

  public boolean isInAppendMode() {
    return append;
  }
}
