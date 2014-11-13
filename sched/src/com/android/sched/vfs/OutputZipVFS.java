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

import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.location.Location;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * A root {@link OutputVDir} backed by a zip archive.
 */
public class OutputZipVFS extends AbstractOutputVFS implements SequentialOutputVFS {
  @Nonnull
  protected final ZipOutputStream zos;
  @Nonnull
  private final   OutputZipFile file;
  @Nonnull
  private final AtomicBoolean lastVFileOpen = new AtomicBoolean(false);

  public OutputZipVFS(@Nonnull OutputZipFile file) {
    setRootDir(new OutputZipVDir(this, new ZipEntry("")));
    zos = file.getOutputStream();
    this.file = file;
  }

  @Override
  public void notifyVFileClosed() {
    boolean previousState = lastVFileOpen.getAndSet(false);
    assert previousState;
  }

  @Override
  public boolean notifyVFileOpenAndReturnPreviousState() {
    return lastVFileOpen.getAndSet(true);
  }

  @Override
  public void close() throws IOException {
    zos.close();
  }

  @Nonnull
  ZipOutputStream getZipOutputStream() {
    return zos;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return file.getLocation();
  }
}
