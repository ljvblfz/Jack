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

import com.google.common.io.Files;

import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.stream.ByteStreamSucker;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * The root of a VFS backed by a real filesystem directory, compressed into a zip archive when
 * closed.
 */
public class InputOutputZipRootVDir extends InputOutputZipVDir implements InputRootVDir,
    InputOutputVDir, Closeable {

  @Nonnull
  private final ZipOutputStream zos;

  public InputOutputZipRootVDir(@Nonnull OutputZipFile zipFile) {
    super(Files.createTempDir(), zipFile, new ZipEntry(""));
    zos = zipFile.getOutputStream();
  }

  @Nonnull
  @Override
  public String getName() {
    return "";
  }

  @Override
  public void close() throws IOException {
    try {
      addDirToZip(zos, this);
    } finally {
      zos.close();
      FileUtils.deleteDir(dir);
    }
  }

  private void addDirToZip(@Nonnull ZipOutputStream zos, @Nonnull InputOutputZipVDir vDir)
      throws IOException {
    for (InputVElement sub : vDir.list()) {
      if (sub.isVDir()) {
        addDirToZip(zos, (InputOutputZipVDir) sub);
      } else {
        InputOutputZipVFile vFile = (InputOutputZipVFile) sub;
        zos.putNextEntry(vFile.getZipEntry());
        InputStream is = vFile.openRead();
        try {
          new ByteStreamSucker(is, zos).suck();
        } finally {
          is.close();
        }
      }
    }
  }
}
