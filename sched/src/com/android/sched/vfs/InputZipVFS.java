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
import com.android.sched.util.location.ZipLocation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Virtual directory for viewing the content of a zip file.
 */
public class InputZipVFS extends AbstractInputVFS {
  @Nonnull
  private final ZipFile zip;
  @Nonnull
  private final Location location;
  private static final Splitter splitter = Splitter.on(ZipUtils.IN_ZIP_SEPARATOR);

  public InputZipVFS(@Nonnull InputZipFile zipFile) {
    setRootDir(new InputZipVDir(this, new ZipEntry("")));
    this.zip  = zipFile.getZipFile();
    this.location = zipFile.getLocation();

    fillSubElements(zip, null);
  }

  public InputZipVFS(@Nonnull InputZipFile zipFile, @Nonnull String prefix) {
    setRootDir(new InputZipVDir(this, new ZipEntry(prefix)));
    this.zip  = zipFile.getZipFile();
    this.location = new ZipLocation(zipFile.getLocation(), new ZipEntry(prefix));

    assert prefix.endsWith("" + ZipUtils.IN_ZIP_SEPARATOR);
    fillSubElements(zip, prefix);
  }

  private void fillSubElements(@Nonnull ZipFile zip, @CheckForNull String prefix) {

    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        if (prefix == null || entryName.startsWith(prefix)) {
          InputZipVDir dir = getRootInputVDir();
          StringBuilder inZipPath = new StringBuilder();
          String relativePath;
          if (prefix != null) {
            relativePath = entryName.substring(prefix.length(), entryName.length());
            inZipPath.append(prefix);
          } else {
            relativePath = entryName;
          }
          Iterator<String> names = splitter.split(relativePath).iterator();

          String simpleName = null;
          while (names.hasNext()) {
            simpleName = names.next();
            assert !simpleName.isEmpty();
            if (names.hasNext()) {
              // simpleName is a dir name
              inZipPath.append(simpleName).append(ZipUtils.IN_ZIP_SEPARATOR);
              InputZipVDir nextDir = (InputZipVDir) dir.subs.get(simpleName);
              if (nextDir == null) {
                // VDir does not already exist
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
    return location;
  }
}
