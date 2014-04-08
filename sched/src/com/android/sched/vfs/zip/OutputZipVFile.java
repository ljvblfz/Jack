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

package com.android.sched.vfs.zip;

import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.util.stream.UncloseableOutputStream;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.OutputVFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

class OutputZipVFile extends AbstractVElement implements OutputVFile {

  @Nonnull
  private final ZipOutputStream zos;
  @Nonnull
  private final ZipEntry entry;
  @Nonnull
  private final Location location;

  OutputZipVFile(@Nonnull ZipOutputStream zos, @Nonnull ZipEntry entry, @Nonnull File zipFile) {
    this.zos = zos;
    this.entry = entry;
    location = new ZipLocation(new FileLocation(zipFile), entry);
  }

  @Nonnull
  @Override
  public String getName() {
    return entry.getName();
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws IOException {
    zos.putNextEntry(entry);
    return new UncloseableOutputStream(zos);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

}
