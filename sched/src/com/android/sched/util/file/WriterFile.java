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
import com.android.sched.util.LineSeparator;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.CustomPrintWriter;
import com.android.sched.util.stream.QueryableOutputStream;
import com.android.sched.util.stream.UncloseableOutputStream;
import com.android.sched.vfs.PrintWriterProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class representing a output stream from a file path or a standard output.
 */
public class WriterFile extends AbstractStreamFile implements PrintWriterProvider {
  @Nonnull
  private static final LineSeparator LINE_SEPARATOR = LineSeparator.SYSTEM;
  @Nonnegative
  private static final int BUFFER_SIZE = 1024 * 8;

  @CheckForNull
  private CustomPrintWriter writer;
  @Nonnull
  private final Charset charset;
  @Nonnull
  private final LineSeparator lineSeparator;
  @Nonnegative
  private final int bufferSize;
  private final boolean append;

  public WriterFile(@Nonnull String name,
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
    this(new File(name), Charset.defaultCharset(), LINE_SEPARATOR, new FileLocation(name),
        hooks, existence, change, append);
  }

  public WriterFile(@Nonnull String name,
      @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator,
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
    this(new File(name), charset, lineSeparator, new FileLocation(name), hooks, existence, change,
        append);
  }

  public WriterFile(@CheckForNull Directory workingDirectory,
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
        Charset.defaultCharset(),
        LINE_SEPARATOR,
        new FileLocation(name),
        hooks,
        existence,
        change,
        append);
  }

  public WriterFile(@CheckForNull Directory workingDirectory,
      @Nonnull String name,
      @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator,
      @Nonnegative int bufferSize,
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
    super(getFileFromWorkingDirectory(workingDirectory, name), new FileLocation(name), hooks);

    performChecks(existence, Permission.WRITE, change);

    this.append = append;
    this.charset = charset;
    this.lineSeparator = lineSeparator;
    this.bufferSize = bufferSize;
  }

  public WriterFile(@CheckForNull Directory workingDirectory,
      @Nonnull String name,
      @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator,
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
        charset,
        lineSeparator,
        new FileLocation(name),
        hooks,
        existence,
        change,
        append);
  }

  protected WriterFile(@Nonnull File file,
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
    this(file, Charset.defaultCharset(), LINE_SEPARATOR, location, hooks, existence, change,
        append);
  }

  protected WriterFile(@Nonnull File file,
      @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator,
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
    this.charset = charset;
    this.lineSeparator = lineSeparator;
    this.bufferSize = BUFFER_SIZE;
  }

  /**
   * Creates a new instance of {@link WriterFile} assuming the file may exist or not, without
   * modifying its permissions. If the file already exists it will be overwritten.
   */
  public WriterFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks)
      throws CannotCreateFileException,
      WrongPermissionException,
      NotFileException {
    this(name, Charset.defaultCharset(), LINE_SEPARATOR, hooks);
  }

  /**
   * Creates a new instance of {@link WriterFile} assuming the file may exist or not, without
   * modifying its permissions. If the file already exists it will be overwritten.
   */
  public WriterFile(@Nonnull String name,
      @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeperator,
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

    this.charset = charset;
    this.append = false;
    this.lineSeparator = lineSeperator;
    this.bufferSize = BUFFER_SIZE;
  }

  /**
   * Creates a new instance of {@link WriterFile} assuming the file must exist, without
   * modifying its permissions. It will be overwritten.
   */
  public WriterFile(@Nonnull String name)
      throws WrongPermissionException, NotFileException {
    this(name, Charset.defaultCharset(), LINE_SEPARATOR);
  }

  /**
   * Creates a new instance of {@link WriterFile} assuming the file must exist, without
   * modifying its permissions. It will be overwritten.
   */
  public WriterFile(@Nonnull String name,
                    @Nonnull Charset charset,
                    @Nonnull LineSeparator lineSeparator)
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

    this.charset = charset;
    this.append = false;
    this.lineSeparator = lineSeparator;
    this.bufferSize = BUFFER_SIZE;
  }

  public WriterFile(@Nonnull StandardOutputKind standardOutputKind) {
    this(standardOutputKind, Charset.defaultCharset(), LineSeparator.SYSTEM);
  }

  public WriterFile(@Nonnull StandardOutputKind standardOutputKind, @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator) {
    this(standardOutputKind.getOutputStream(), charset, lineSeparator,
        standardOutputKind.getLocation());
  }

  public WriterFile(@Nonnull OutputStream stream, @Nonnull Location location) {
    this(stream, Charset.defaultCharset(), LINE_SEPARATOR, location);
  }

  public WriterFile(@Nonnull OutputStream stream, @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator, @Nonnegative int bufferSize,
      @Nonnull Location location) {
    super(location);
    this.charset = charset;
    this.append = true;
    this.lineSeparator = lineSeparator;
    this.bufferSize = bufferSize;
    this.stream = new QueryableOutputStream(new UncloseableOutputStream(stream));
    this.writer = getCustomPrintWriter((OutputStream) this.stream);
  }

  public WriterFile(@Nonnull OutputStream stream, @Nonnull Charset charset,
      @Nonnull LineSeparator lineSeparator, @Nonnull Location location) {
    super(location);
    this.charset = charset;
    this.append = true;
    this.lineSeparator = lineSeparator;
    this.bufferSize = BUFFER_SIZE;
    this.stream = new QueryableOutputStream(new UncloseableOutputStream(stream));
    this.writer = getCustomPrintWriter((OutputStream) this.stream);
  }

  @Override
  @Nonnull
  @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
  public synchronized CustomPrintWriter getPrintWriter() {
    wasUsed = true;
    if (writer == null) {
      clearRemover();

      try {
        this.stream = new QueryableOutputStream(new FileOutputStream(file, append));
        this.writer = getCustomPrintWriter((OutputStream) this.stream);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return writer;
  }

  @Nonnull
  private CustomPrintWriter getCustomPrintWriter(@Nonnull OutputStream os) {
    Writer tmp = new OutputStreamWriter(os, charset);
    if (bufferSize > 0) {
      tmp = new BufferedWriter(tmp, bufferSize);
    }

    return new CustomPrintWriter(tmp, lineSeparator.getLineSeparator());
  }


  public boolean isInAppendMode() {
    return append;
  }

  @Nonnull
  public Charset getCharset() {
    return charset;
  }

  @Nonnull
  public LineSeparator getLineSeparator() {
    return lineSeparator;
  }
}
