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

import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VElement;
import com.android.sched.vfs.VPath;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * A root {@link OutputVDir} backed by a zip archive.
 */
public class OutputZipRootVDir extends AbstractVElement implements OutputVDir, Closeable {

  @Nonnull
  protected final HashMap<String, VElement> subs = new HashMap<String, VElement>();
  @Nonnull
  private final Location location;
  @Nonnull
  protected final ZipOutputStream zos;
  @Nonnull
  private final String fileName;
  @Nonnull
  private final OutputZipFile zipFile;

  public OutputZipRootVDir(@Nonnull OutputZipFile zipFile) {
    fileName = zipFile.getName();
    location = new ZipLocation(zipFile.getLocation(), new ZipEntry(""));
    zos = zipFile.getOutputStream();
    this.zipFile = zipFile;
  }

  @Nonnull
  @Override
  public String getName() {
    return fileName;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) {
    return new OutputZipVFile(zos, new ZipEntry(path.getPathAsString(getSeparator())), zipFile);
  }

  @Override
  public void close() throws IOException {
    zos.close();
  }

  @Override
  public char getSeparator() {
    return '/';
  }

  @Override
  public boolean isVDir() {
    return true;
  }
}
