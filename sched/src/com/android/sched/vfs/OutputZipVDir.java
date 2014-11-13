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

import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

/**
 * A root {@link OutputVDir} backed by a zip archive.
 */
public class OutputZipVDir extends AbstractVElement implements OutputVDir {
  @Nonnull
  private final OutputZipVFS vfs;
  @Nonnull
  private final ZipEntry     entry;

  public OutputZipVDir(@Nonnull OutputZipVFS vfs, @Nonnull ZipEntry entry) {
    this.vfs = vfs;
    this.entry = entry;
  }

  @Override
  @Nonnull
  public String getName() {
    return ZipUtils.getSimpleName(entry);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), entry);
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) {
    assert !(path.equals(VPath.ROOT));
    String newEntryName = path.getPathAsString(ZipUtils.IN_ZIP_SEPARATOR);
    String parentEntryName = entry.getName();
    if (!parentEntryName.isEmpty()) {
      newEntryName = parentEntryName + ZipUtils.IN_ZIP_SEPARATOR + newEntryName;
    }
    return new OutputZipVFile(vfs, new ZipEntry(newEntryName));
  }

  @Override
  public boolean isVDir() {
    return true;
  }
}
