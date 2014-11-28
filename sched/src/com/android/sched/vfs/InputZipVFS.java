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

import com.google.common.base.Splitter;

import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.location.Location;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Virtual directory for viewing the content of a zip file.
 */
public class InputZipVFS extends AbstractInputVFS {
  @Nonnull
  private final ZipFile zip;
  @Nonnull
  private final InputZipFile file;

  public InputZipVFS(@Nonnull InputZipFile zipFile) {
    setRootDir(new InputZipVDir(this, new ZipEntry("")));
    this.zip  = zipFile.getZipFile();
    this.file = zipFile;

    Splitter splitter = Splitter.on(ZipUtils.IN_ZIP_SEPARATOR);
    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        Iterator<String> names = splitter.split(entryName).iterator();
        InputZipVDir dir = getRootInputVDir();
        StringBuilder inZipPath = new StringBuilder();
        String simpleName = null;
        while (names.hasNext()) {
          simpleName = names.next();
          if (names.hasNext()) {
            inZipPath.append(ZipUtils.IN_ZIP_SEPARATOR).append(simpleName);
            InputZipVDir nextDir = (InputZipVDir) dir.subs.get(simpleName);
            if (nextDir == null) {
              nextDir = new InputZipVDir(this, new ZipEntry(inZipPath.toString()));
              dir.subs.put(simpleName, nextDir);
            }
            dir = nextDir;
          }
        }
        dir.subs.put(simpleName, new InputZipVFile(this, entry));
      }
    }
  }

  @Nonnull
  ZipFile getZipFile() {
    return zip;
  }

  @Override
  @Nonnull
  public InputZipVDir getRootInputVDir() {
    return (InputZipVDir) super.getRootInputVDir();
  }

  @Override
  public void close() throws IOException {
    zip.close();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return file.getLocation();
  }
}
