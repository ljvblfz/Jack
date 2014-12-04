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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

class OutputZipVFile extends AbstractVElement implements OutputVFile {
  @Nonnull
  private final OutputZipVFS vfs;
  @Nonnull
  private final ZipEntry     entry;

  OutputZipVFile(@Nonnull OutputZipVFS vfs, @Nonnull ZipEntry entry) {
    this.vfs = vfs;
    this.entry = entry;
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws IOException {
    vfs.getZipOutputStream().putNextEntry(entry);
    if (vfs.notifyVFileOpenAndReturnPreviousState()) {
      throw new AssertionError(getLocation().getDescription()
          + " cannot be written to because a previous stream has not been closed.");
    }

    return new UnclosableVFileOutputStream(vfs);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), entry);
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  @Override
  @Nonnull
  public String getName() {
    return ZipUtils.getFileSimpleName(entry);
  }

  private static class UnclosableVFileOutputStream extends FilterOutputStream {
    @Nonnull
    private final OutputZipVFS vfs;

    public UnclosableVFileOutputStream(@Nonnull OutputZipVFS vfs) {
      super(vfs.getZipOutputStream());
      this.vfs = vfs;
    }

    @Override
    public void close() {
      // we do not actually close the stream
      vfs.notifyVFileClosed();
    }

    @Override
    public void write(byte[] b) throws IOException {
      out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      out.write(b, off, len);
    }
  }
}
