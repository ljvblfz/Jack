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

import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

class InputZipVDir extends AbstractVElement implements InputVDir {

  @Nonnull
  private final  InputZipVFS vfs;
  @Nonnull
  protected final HashMap<String, InputVElement> subs = new HashMap<String, InputVElement>();
  @Nonnull
  private final ZipEntry entry;

  InputZipVDir(@Nonnull InputZipVFS vfs, @Nonnull ZipEntry entry) {
    this.vfs  = vfs;
    this.entry  = entry;
  }

  @Nonnull
  @Override
  public String getName() {
    return ZipUtils.getDirSimpleName(entry);
  }

  @Nonnull
  @Override
  public Collection<? extends InputVElement> list() {
    return subs.values();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), entry);
  }

  @Override
  public boolean isVDir() {
    return true;
  }

  @Override
  @Nonnull
  public InputVFile getInputVFile(@Nonnull VPath path) throws NotFileOrDirectoryException,
      NoSuchFileException {
    Iterator<String> iterator = path.split().iterator();
    assert iterator.hasNext();
    String firstElement = iterator.next();
    InputVElement ive = subs.get(firstElement);
    String pathAsString = path.getPathAsString(ZipUtils.IN_ZIP_SEPARATOR);

    if (ive == null) {
      throw new NoSuchFileException(new FileLocation(pathAsString));
    }

    if (iterator.hasNext()) {
      if (ive instanceof InputVDir) {
        ive = ((InputVDir) ive).getInputVFile(new VPath(
            pathAsString.substring(pathAsString.indexOf(ZipUtils.IN_ZIP_SEPARATOR) + 1),
            ZipUtils.IN_ZIP_SEPARATOR));
      }
    }

    if (ive.isVDir()) {
      throw new NotFileOrDirectoryException(new DirectoryLocation(pathAsString));
    }

    return (InputVFile) ive;
  }

  @Override
  @Nonnull
  public InputVDir getInputVDir(@Nonnull VPath path) throws NotFileOrDirectoryException,
      NoSuchFileException {
    if (path.equals(VPath.ROOT)) {
      return this;
    }

    Iterator<String> iterator = path.split().iterator();
    assert iterator.hasNext();
    String firstElement = iterator.next();
    InputVElement ive = subs.get(firstElement);
    String pathAsString = path.getPathAsString(ZipUtils.IN_ZIP_SEPARATOR);

    if (ive == null) {
      throw new NoSuchFileException(new DirectoryLocation(pathAsString));
    }

    if (iterator.hasNext()) {
      if (ive instanceof InputVDir) {
        ive = ((InputVDir) ive).getInputVDir(new VPath(
            pathAsString.substring(pathAsString.indexOf(ZipUtils.IN_ZIP_SEPARATOR) + 1),
            ZipUtils.IN_ZIP_SEPARATOR));
      }
    }

    if (!ive.isVDir()) {
      throw new NotFileOrDirectoryException(new DirectoryLocation(pathAsString));
    }

    return (InputVDir) ive;
  }
}
