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

import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * A {@code VFile} directly backed by a {@code java.io.File}.
 */
public class DirectFile extends AbstractVElement implements InputOutputVFile {

  @Nonnull
  private final File file;
  @Nonnull
  private final InputOutputVDir vfsRoot;

  DirectFile(@Nonnull File file, @Nonnull InputOutputVDir vfsRoot) {
    this.file = file;
    this.vfsRoot = vfsRoot;
  }

  @Nonnull
  @Override
  public InputStream openRead() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws FileNotFoundException {
    if (vfsRoot instanceof SequentialOutputVDir) {
      if (((SequentialOutputVDir) vfsRoot).notifyVFileOpenAndReturnPreviousState()) {
        throw new AssertionError(getLocation().getDescription()
            + " cannot be written to because a previous stream has not been closed.");
      }
    }
    return new VFileOutputStream(new FileOutputStream(file), vfsRoot);
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new FileLocation(file);
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  private static class VFileOutputStream extends FilterOutputStream {

    private final OutputVDir vfsRoot;

    public VFileOutputStream(@Nonnull OutputStream out, @Nonnull OutputVDir vfsRoot) {
      super(out);
      this.vfsRoot = vfsRoot;
    }

    @Override
    public void close() throws IOException {
      super.close();
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
