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

package com.android.sched.util.location;

import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;


/**
 * Class describing an entry in an archive.
 */
public class ZipLocation extends Location {

  @Nonnull
  private final Location archive;

  @Nonnull
  private final String entryName;

  public ZipLocation(@Nonnull Location archive, @Nonnull ZipEntry entry) {
    this.archive = archive;
    this.entryName = entry.getName();
  }

  @Override
  @Nonnull
  public String getDescription() {
    StringBuilder sb = new StringBuilder();

    if (!archive.getDescription().isEmpty()) {
      sb.append(archive.getDescription()).append(", ");
    }

    return sb.append("entry '/").append(entryName).append('\'').toString();
  }

  @Nonnull
  public Location getArchive() {
    return archive;
  }

  @Nonnull
  public String getEntryName() {
    return entryName;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof ZipLocation
        && ((ZipLocation) obj).archive.equals(archive)
        && ((ZipLocation) obj).entryName.equals(entryName);
  }

  @Override
  public final int hashCode() {
    return archive.hashCode() ^ entryName.hashCode();
  }
}
