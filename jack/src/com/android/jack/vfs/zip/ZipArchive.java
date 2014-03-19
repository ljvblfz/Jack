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

package com.android.jack.vfs.zip;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Virtual directory for viewing the content of a zip file.
 */
public class ZipArchive extends ZipDir implements Closeable {

  @Nonnull
  public static final String IN_ZIP_SEPARATOR = "/";

  @Nonnull
  private final ZipFile zip;

  public ZipArchive(@Nonnull File zipFile) throws IOException {
    super("", zipFile, new ZipEntry(""));

    zip = new ZipFile(zipFile);

    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        String[] names = entryName.split(IN_ZIP_SEPARATOR);
        @SuppressWarnings("resource")
        ZipDir dir = this;
        StringBuilder inZipPath = new StringBuilder();
        for (int i = 0; i < names.length - 1; i++) {
          String simpleName = names[i];
          inZipPath.append(IN_ZIP_SEPARATOR).append(simpleName);
          ZipDir nextDir = (ZipDir) dir.subs.get(simpleName);
          if (nextDir == null) {
            nextDir = new ZipDir(simpleName, zipFile, new ZipEntry(inZipPath.toString()));
            dir.subs.put(simpleName, nextDir);
          }
          dir = nextDir;
        }
        String simpleName = names[names.length - 1];
        dir.subs.put(simpleName, new ZipVFile(simpleName, zip, entry));
      }
    }
  }

  @Override
  public void close() throws IOException {
    zip.close();
  }
}
