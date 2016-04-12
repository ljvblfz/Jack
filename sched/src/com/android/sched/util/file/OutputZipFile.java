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
import com.android.sched.util.stream.QueryableStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a zip file designed to be written to.
 */
public class OutputZipFile extends OutputStreamFile {
  /**
   * Whether the zip will be compressed.
   */
  public enum Compression {
    COMPRESSED, UNCOMPRESSED
  }

  @Nonnull
  private final Compression compression;

  public OutputZipFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      @Nonnull Compression compression)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    this(new File(name), new FileLocation(name), hooks, existence, change, compression);
  }

  public OutputZipFile(@CheckForNull Directory workingDirectory,
      @Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      @Nonnull Compression compression)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    this(getFileFromWorkingDirectory(workingDirectory, name),
        new FileLocation(name), hooks, existence, change, compression);
  }

  private OutputZipFile(@Nonnull File file,
      @Nonnull FileLocation location,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      @Nonnull ChangePermission change,
      @Nonnull Compression compression)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    super(file, location, hooks, existence, change, false);
    this.compression = compression;
  }

  @Override
  @Nonnull
  public synchronized ZipOutputStream getOutputStream() {
    assert file != null;

    wasUsed = true;
    if (stream == null) {
      clearRemover();
      try {
        stream = new CustomZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)),
            compression);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return (ZipOutputStream) stream;
  }

  @Nonnull
  public String getName() {
    assert file != null;
    return file.getName();
  }

  /**
   * A {@link ZipOutputStream} that is not directly closed to avoid getting a {@link ZipException}
   * when the zip has no entry (with a JRE 6) and implements {@link QueryableStream}.
   */
  private static class CustomZipOutputStream extends ZipOutputStream implements QueryableStream {

    private boolean hasEntries = false;
    private boolean isClosed = false;

    public CustomZipOutputStream(@Nonnull OutputStream out, @Nonnull Compression compression) {
      super(out);
      switch (compression) {
        case COMPRESSED:
          setMethod(ZipOutputStream.DEFLATED);
          break;
        case UNCOMPRESSED:
          setMethod(ZipOutputStream.DEFLATED);
          setLevel(Deflater.NO_COMPRESSION);
          break;
        default:
          throw new AssertionError(compression.name());
      }
    }

    @Override
    public void putNextEntry(@Nonnull ZipEntry e) throws IOException {
      hasEntries = true;
      super.putNextEntry(e);
    }

    @Override
    public synchronized void close() throws IOException {
      if (hasEntries) {
        super.close();
      } else {
        out.close();
      }
      isClosed = true;
    }

    @Override
    public synchronized boolean isClosed() {
      return isClosed;
    }
  }
}