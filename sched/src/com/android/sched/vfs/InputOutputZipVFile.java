/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A file in an {@link InputOutputZipVFS} VFS.
 */
class InputOutputZipVFile extends AbstractVElement implements InputOutputVFile {

  @Nonnull
  private final File file;
  @CheckForNull
  private ArrayList<InputVElement> list;
  @Nonnull
  private final InputOutputZipVFS vfs;
  @Nonnull
  private final ZipEntry zipEntry;

  public InputOutputZipVFile(@Nonnull InputOutputZipVFS vfs, @Nonnull File file,
      @Nonnull ZipEntry zipEntry) {
    this.vfs = vfs;
    this.zipEntry = zipEntry;
    this.file = file;
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), zipEntry);
  }

  @Override
  public boolean isVDir() {
    return false;
  }


  @Nonnull
  @Override
  public InputStream openRead() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws FileNotFoundException {
    assert !vfs.isClosed();
    return new FileOutputStream(file);
  }

  @Nonnull
  public ZipEntry getZipEntry() {
    return zipEntry;
  }
}
