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

package com.android.jack.library;

import com.android.sched.util.location.FileLocation;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An {@link InputLibrary} that represents an invalid library.
 */
public class InvalidLibrary implements InputLibrary {

  @Nonnull
  private final File file;

  @Nonnull
  private final List<Throwable> causes;

  public InvalidLibrary(@Nonnull File file, @Nonnull List<Throwable> causes) {
    this.file = file;
    this.causes = causes;
  }

  @Nonnull
  public List<Throwable> getInvalidCauses() {
    return causes;
  }

  @Override
  public void close() {
  }

  @Override
  public boolean isClosed() {
    return true;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public int getMajorVersion() {
    return 0;
  }

  @Override
  @Nonnull
  public String getPath() {
    return file.getPath();
  }

  @Override
  @Nonnull
  public Collection<FileType> getFileTypes() {
    return Collections.emptyList();
  }

  @Override
  public boolean containsFileType(@Nonnull FileType fileType) {
    return false;
  }

  @Override
  @Nonnull
  public InputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
  }

  @Override
  @Nonnull
  public InputVDir getDir(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
  }

  @Override
  @Nonnull
  public Iterator<InputVFile> iterator(@Nonnull FileType fileType) {
    return Collections.<InputVFile>emptyList().iterator();
  }

  @Override
  @Nonnull
  public LibraryLocation getLocation() {
    return new LibraryLocation(new FileLocation(file));
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return null;
  }

  @Override
  @Nonnull
  public void delete(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
  }
}
