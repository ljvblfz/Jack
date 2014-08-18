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
import com.android.sched.util.location.ZipLocation;

import java.io.FilterOutputStream;
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
  @Nonnull
  private final OutputVDir vfsRoot;

  OutputZipVFile(@Nonnull ZipOutputStream zos, @Nonnull ZipEntry entry,
      @Nonnull OutputZipFile zipFile, @Nonnull OutputVDir vfsRoot) {
    this.zos = zos;
    this.entry = entry;
    location = new ZipLocation(zipFile.getLocation(), entry);
    this.vfsRoot = vfsRoot;
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws IOException {
    zos.putNextEntry(entry);
    if (vfsRoot instanceof SequentialOutputVDir) {
      if (((SequentialOutputVDir) vfsRoot).notifyVFileOpenAndReturnPreviousState()) {
        throw new AssertionError(getLocation().getDescription()
            + " cannot be written to because a previous stream has not been closed.");
      }
    }
    return new UnclosableVFileOutputStream(zos, vfsRoot);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  private static class UnclosableVFileOutputStream extends FilterOutputStream {

    private final OutputVDir vfsRoot;

    public UnclosableVFileOutputStream(@Nonnull OutputStream out, @Nonnull OutputVDir vfsRoot) {
      super(out);
      this.vfsRoot = vfsRoot;
    }

    @Override
    public void close() {
      // we do not actually close the stream
      if (vfsRoot instanceof SequentialOutputVDir) {
        ((SequentialOutputVDir) vfsRoot).notifyVFileClosed();
      }
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
