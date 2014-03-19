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

import com.android.sched.util.RunnableHooks;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.UncloseableInputStream;
import com.android.sched.util.stream.UncloseableOutputStream;
import com.android.sched.util.stream.UncloseablePrintStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a stream from a file path or a standard input/output.
 */
public class StreamFile extends FileOrDirectory {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @CheckForNull
  protected final File   file;

  private final int     permissions;
  private final boolean append;

  public StreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      int permissions,
      @Nonnull ChangePermission change,
      boolean append)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException {
    super(name, hooks);

    this.file = new File(name);
    this.permissions = permissions;
    this.append = append;

    if (existence == Existence.MAY_EXIST) {
      if (file.exists()) {
        existence = Existence.MUST_EXIST;
      } else {
        existence = Existence.NOT_EXIST;
      }
    }

    switch (existence) {
      case MUST_EXIST:
        processExisting(file, name, permissions);
        break;
      case NOT_EXIST:
        processNotExisting(file, name, permissions, change);
        break;
      case MAY_EXIST:
        throw new AssertionError();
    }
  }

  public StreamFile() {
    super("<standard>", null);
    this.file = null;
    this.permissions = 0;
    this.append = true;
  }

  public boolean isStandard() {
    return this.file == null;
  }

  @Nonnull
  public InputStream getInputStream() throws FileNotFoundException {
    if (file == null) {
      return new UncloseableInputStream(System.in);
    } else {
      assert isReadable();

      clearRemover();
      return new FileInputStream(file);
    }
  }

  @Nonnull
  public OutputStream getOutputStream() throws FileNotFoundException {
    if (file == null) {
      return new UncloseableOutputStream(System.out);
    } else {
      assert isWritable();

      clearRemover();
      return new FileOutputStream(file, append);
    }
  }

  @Nonnull
  public PrintStream getPrintStream() throws FileNotFoundException {
    if (file == null) {
      return new UncloseablePrintStream(System.out);
    } else {
      assert isWritable();

      clearRemover();
      return new PrintStream(file);
    }
  }

  private boolean isReadable() {
    return (permissions & Permission.READ) != 0;
  }

  private boolean isWritable() {
    return (permissions & Permission.WRITE) != 0;
  }

  @Override
  @Nonnull
  public String toString() {
    return getName();
  }

  private void processNotExisting(
      @Nonnull File file, @Nonnull String name, int permissions, @Nonnull ChangePermission change)
      throws FileAlreadyExistsException, CannotCreateFileException, CannotSetPermissionException,
      WrongPermissionException {
    // Check existing
    if (file.exists()) {
      throw new FileAlreadyExistsException(name, /* isFile */ true);
    }

    // Create file
    try {
      if (file.createNewFile()) {
        logger.log(Level.FINE, "Create file ''{0}'' (''{1}'')",
            new Object[] {name, file.getAbsoluteFile()});
        addRemover(file);
      } else {
        throw new CannotCreateFileException(name, /* isFile */ true);
      }
    } catch (IOException e) {
      throw new CannotCreateFileException(name, /* isFile */ true, e);
    }

    setPermissions(file, name, permissions, change);
    checkPermissions(file, name, permissions);
  }

  private void processExisting(@Nonnull File file, @Nonnull String name, int permissions)
      throws NoSuchFileException, NotFileOrDirectoryException, WrongPermissionException {
    // Check existing
    if (!file.exists()) {
      throw new NoSuchFileException(name, /* isFile */ true);
    }

    // Check it is a file
    if (!file.isFile()) {
      throw new NotFileOrDirectoryException(name, /* isFile */ true);
    }

    checkPermissions(file, name, permissions);
  }
}