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

package com.android.sched.vfs;

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An {@link InputOutputVFile} implementation for a {@link GenericInputOutputVFS}.
 */
public class GenericInputOutputVFile implements InputOutputVFile {
  @Nonnull
  private final VFile file;

  GenericInputOutputVFile(@Nonnull VFile file) {
    this.file = file;
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  @Override
  @Nonnull
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return file.getLocation();
  }

  @Override
  @Nonnull
  public InputStream getInputStream() throws WrongPermissionException {
    return file.getInputStream();
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream() throws WrongPermissionException {
    return getOutputStream(false);
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream(boolean append) throws WrongPermissionException {
    return file.getOutputStream(append);
  }

  @Override
  @Nonnull
  public PrintStream getPrintStream(boolean append) throws WrongPermissionException {
    return new PrintStream(getOutputStream(append));
  }

  @Override
  public void delete() throws CannotDeleteFileException {
    file.delete();
  }

  @CheckForNull
  public String getDigest() {
    return file.getDigest();
  }

  @Override
  @Nonnull
  public VPath getPathFromRoot() {
    return file.getPathFromRoot();
  }

  @Override
  public long getLastModified() {
    return file.getLastModified();
  }

  @Override
  @Nonnull
  public VFile getVFile() {
    return file;
  }

  @Override
  public void copy(@Nonnull InputVFile inputFile) throws WrongPermissionException,
      CannotCloseException, CannotReadException, CannotWriteException {
    file.copy(inputFile.getVFile());
  }
}