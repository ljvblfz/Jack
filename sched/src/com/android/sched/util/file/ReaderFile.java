/*
 * Copyright (C) 2016 The Android Open Source Project
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
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.StandardInputLocation;
import com.android.sched.util.stream.QueryableInputStream;
import com.android.sched.util.stream.UncloseableInputStream;
import com.android.sched.vfs.ReaderProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class representing a input stream from a file path or a standard input.
 */
public class ReaderFile extends AbstractStreamFile implements ReaderProvider {
  @Nonnegative
  private static final int BUFFER_SIZE = 1024 * 8;

  @CheckForNull
  private BufferedReader reader;
  @Nonnull
  private final Charset charset;
  @Nonnegative
  private final int bufferSize;

  public ReaderFile(@Nonnull String name)
      throws WrongPermissionException, NotFileException, NoSuchFileException {
    this(new File(name), Charset.defaultCharset(), new FileLocation(name));
  }

  public ReaderFile(@Nonnull String name, @Nonnull Charset charset)
      throws WrongPermissionException, NotFileException, NoSuchFileException {
    this(new File(name), charset, new FileLocation(name));
  }

  public ReaderFile() {
    this(Charset.defaultCharset());
  }

  public ReaderFile(@Nonnull Charset charset) {
    super(new StandardInputLocation());
    this.charset = charset;
    this.bufferSize = BUFFER_SIZE;
    this.stream = new QueryableInputStream(new UncloseableInputStream(System.in));
    this.reader =
        new BufferedReader(new InputStreamReader((InputStream) this.stream, charset), bufferSize);
  }

  public ReaderFile(@Nonnull InputStream stream, @Nonnull Location location) {
    this(stream, Charset.defaultCharset(), location);
  }

  public ReaderFile(@Nonnull InputStream stream, @Nonnull Charset charset,
      @Nonnull Location location) {
    super(location);
    this.charset = charset;
    this.bufferSize = BUFFER_SIZE;
    this.stream = new QueryableInputStream(new UncloseableInputStream(System.in));
    this.reader =
        new BufferedReader(new InputStreamReader((InputStream) this.stream, charset), bufferSize);
  }

  public ReaderFile(@Nonnull InputStream stream, @Nonnull Charset charset,
      @Nonnegative int bufferSize, @Nonnull Location location) {
    super(location);
    this.charset = charset;
    this.bufferSize = bufferSize;
    this.stream = new QueryableInputStream(new UncloseableInputStream(System.in));
    this.reader =
        new BufferedReader(new InputStreamReader((InputStream) this.stream, charset), bufferSize);
  }

  public ReaderFile(@CheckForNull Directory workingDirectory, @Nonnull String string)
      throws NotFileException, WrongPermissionException, NoSuchFileException {
    this(getFileFromWorkingDirectory(workingDirectory, string), Charset.defaultCharset(),
        new FileLocation(string));
  }

  public ReaderFile(@CheckForNull Directory workingDirectory, @Nonnull String string,
      @Nonnull Charset charset)
          throws NotFileException, WrongPermissionException, NoSuchFileException {
    this(getFileFromWorkingDirectory(workingDirectory, string), charset, new FileLocation(string));
  }

  public ReaderFile(@CheckForNull Directory workingDirectory, @Nonnull String string,
      @Nonnull Charset charset, @Nonnegative int bufferSize)
          throws NotFileException, WrongPermissionException, NoSuchFileException {
    super(getFileFromWorkingDirectory(workingDirectory, string), new FileLocation(string),
        /* hooks = */ null);

    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }

    this.charset = charset;
    this.bufferSize = bufferSize;
  }

  private ReaderFile(@Nonnull File file, @Nonnull Charset charset, @Nonnull FileLocation location)
      throws WrongPermissionException, NotFileException, NoSuchFileException {
    super(file, location, null /* hooks */);

    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }

    this.charset = charset;
    this.bufferSize = BUFFER_SIZE;
  }

  @Override
  @Nonnull
  public synchronized BufferedReader getBufferedReader() {
    wasUsed = true;
    if (reader == null) {
      clearRemover();

      try {
        this.stream = new QueryableInputStream(new FileInputStream(file));
        this.reader = new BufferedReader(new InputStreamReader((InputStream) this.stream, charset),
            bufferSize);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return reader;
  }

  @Nonnull
  public Charset getCharset() {
    return charset;
  }
}