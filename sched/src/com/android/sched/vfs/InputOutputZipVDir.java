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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

/**
 * A directory in an {@link InputOutputZipRootVDir} VFS.
 */
class InputOutputZipVDir extends AbstractVElement implements InputOutputVDir {

  @Nonnull
  private static final char ZIP_ENTRY_SEPARATOR = '/';
  @Nonnull
  protected final File dir;
  @Nonnull
  private final Location location;
  @Nonnull
  protected final OutputZipFile zipFile;
  @Nonnull
  private final ZipEntry zipEntry;

  public InputOutputZipVDir(@Nonnull File dir, @Nonnull OutputZipFile zipFile,
      @Nonnull ZipEntry zipEntry) {
    location = new ZipLocation(zipFile.getLocation(), zipEntry);
    this.zipFile = zipFile;
    this.dir = dir;
    this.zipEntry = zipEntry;
  }

  @Nonnull
  @Override
  public String getName() {
    return dir.getName();
  }

  @Nonnull
  @Override
  public synchronized Collection<? extends InputVElement> list() {
    File[] subs = dir.listFiles();
    if (subs == null) {
      throw new ConcurrentIOException(new ListDirException(dir));
    }
    if (subs.length == 0) {
      return Collections.emptyList();
    }

    ArrayList<InputVElement> items = new ArrayList<InputVElement>(subs.length);
    for (File sub : subs) {
      String zipEntryName = zipEntry.getName();
      String subZipEntryName;
      if (zipEntryName.isEmpty()) {
        subZipEntryName = sub.getName();
      } else {
        subZipEntryName = zipEntryName + ZIP_ENTRY_SEPARATOR + sub.getName();
      }
      ZipEntry subZipEntry = new ZipEntry(subZipEntryName);
      if (sub.isFile()) {
        items.add(new InputOutputZipVFile(sub, zipFile, subZipEntry));
      } else {
        items.add(new InputOutputZipVDir(sub, zipFile, subZipEntry));
      }
    }

    return items;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    File file = new File(dir, path.getPathAsString(getSeparator()));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }
    return new InputOutputZipVFile(file, zipFile, new ZipEntry(
        zipEntry.getName() + ZIP_ENTRY_SEPARATOR + path.getPathAsString(ZIP_ENTRY_SEPARATOR)));
  }

  @Override
  public char getSeparator() {
    return File.separatorChar;
  }

  @Override
  public boolean isVDir() {
    return true;
  }
}
