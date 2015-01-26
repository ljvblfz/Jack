/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.ReadZipFS.ZipVDir;
import com.android.sched.vfs.ReadZipFS.ZipVFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} implementation backed by a zip archive that only supports reading.
 */
public class ReadZipFS extends BaseVFS<ZipVDir, ZipVFile> implements VFS {


  static class ZipVDir extends InMemoryVDir {

    @Nonnull
    private final ZipEntry zipEntry;

    ZipVDir(@Nonnull BaseVFS<? extends ZipVDir, ? extends ZipVFile> vfs,
        @Nonnull ZipEntry zipEntry, @Nonnull String name) {
      super(vfs, name);
      this.zipEntry = zipEntry;
    }

    @Nonnull
    public ZipEntry getZipEntry() {
      return zipEntry;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return new VPath(zipEntry.getName(), '/');
    }
  }

  static class ZipVFile extends BaseVFile {

    @Nonnull
    private final ZipEntry zipEntry;

    ZipVFile(
        @Nonnull BaseVFS<? extends ZipVDir, ? extends ZipVFile> vfs,
        @Nonnull ZipEntry zipEntry, @Nonnull String name) {
      super(vfs, name);
      this.zipEntry = zipEntry;
    }

    @Nonnull
    public ZipEntry getZipEntry() {
      return zipEntry;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return new VPath(zipEntry.getName(), '/');
    }
  }

  @Nonnull
  private static final Splitter splitter = Splitter.on(ZipUtils.ZIP_SEPARATOR);
  @Nonnull
  private final ZipVDir root = new ZipVDir(this, new ZipEntry(""), "");
  @Nonnull
  private final InputZipFile inputZipFile;
  @Nonnull
  private final ZipFile zipFile;
  @Nonnull
  private final Set<Capabilities> capabilities;

  public ReadZipFS(@Nonnull InputZipFile zipFile) {
    this.inputZipFile = zipFile;
    this.zipFile = zipFile.getZipFile();
    fillSubElements();

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    capabilities.add(Capabilities.READ);
    capabilities.add(Capabilities.PARALLEL_READ);
    capabilities.add(Capabilities.CASE_SENSITIVE);
    this.capabilities = Collections.unmodifiableSet(capabilities);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "zip archive reader";
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return inputZipFile.getLocation();
  }

  @Override
  @Nonnull
  public String getPath() {
    return inputZipFile.getPath();
  }

  @Override
  @Nonnull
  public ZipVDir getRootDir() {
    return root;
  }

  @Override
  public synchronized void close() throws IOException {
    if (!closed) {
      zipFile.close();
      closed = true;
    }
  }

  //
  // Stream
  //

  @Override
  @Nonnull
  InputStream openRead(@Nonnull ZipVFile file) {
    try {
      return zipFile.getInputStream(file.getZipEntry());
    } catch (IOException e) {
      // TODO(jplesot): Auto-generated catch block
      throw new AssertionError(e);
    }
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull ZipVFile file) {
    throw new UnsupportedOperationException();
  }

  //
  // VElement
  //

  @Override
  @Nonnull
  ZipVDir getVDir(@Nonnull ZipVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  ZipVFile getVFile(@Nonnull ZipVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  ZipVDir createVDir(@Nonnull ZipVDir parent, @Nonnull String name) {
    assert !isClosed();

    return new ZipVDir(this, new ZipEntry(parent.getZipEntry().getName() + name + '/'), name);
  }

  @Override
  @Nonnull
  ZipVFile createVFile(@Nonnull ZipVDir parent, @Nonnull String name) {
    assert !isClosed();

    return new ZipVFile(this, zipFile.getEntry(parent.getZipEntry().getName() + name), name);
  }

  @Override
  @Nonnull
  void delete(@Nonnull ZipVFile file) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull ZipVDir dir) {
    return dir.list();
  }

  @Override
  boolean isEmpty(@Nonnull ZipVDir dir) {
    return dir.list().isEmpty();
  }

  //
  // Location
  //

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVFile file) {
    return new ZipLocation(inputZipFile.getLocation(), file.getZipEntry());
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVDir parent, @Nonnull String name) {
    return new ZipLocation(inputZipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + name));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir dir) {
    return new ZipLocation(inputZipFile.getLocation(), dir.getZipEntry());
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir parent, @Nonnull String name) {
    return new ZipLocation(inputZipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + name + '/'));
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVDir parent, @Nonnull VPath path) {
    return new ZipLocation(inputZipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + path.getPathAsString('/')));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir parent, @Nonnull VPath path) {
    return new ZipLocation(inputZipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + path.getPathAsString('/') + '/'));
  }

  //
  // Misc
  //

  @Override
  public boolean needsSequentialWriting() {
    return false;
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  private void fillSubElements() {

    try {
      for (Enumeration<? extends ZipEntry> entries = zipFile.entries();
          entries.hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          ZipVDir currentDir = getRootDir();
          StringBuilder inZipPath = new StringBuilder();
          Iterator<String> names = splitter.split(entryName).iterator();

          String simpleName = null;
          while (names.hasNext()) {
            simpleName = names.next();
            assert !simpleName.isEmpty();
            if (names.hasNext()) {
              // simpleName is a dir name
              inZipPath.append(simpleName).append(ZipUtils.ZIP_SEPARATOR);
              currentDir = (ZipVDir) currentDir.createVDir(simpleName);
            }
          }
          currentDir.createVFile(simpleName);
        }
      }
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    }
  }
}
