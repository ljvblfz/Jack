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

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.ReadZipFS.ZipVDir;
import com.android.sched.vfs.ReadZipFS.ZipVFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
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

    @Override
    @Nonnull
    public BaseVFile getVFile(@Nonnull String name) throws NoSuchFileException,
        NotFileException {
      return vfs.getVFile(this, name);
    }

    @Override
    @Nonnull
    public BaseVDir getVDir(@Nonnull String name) throws NotDirectoryException,
        NoSuchFileException {
      return vfs.getVDir(this, name);
    }

    @Override
    @Nonnull
    public BaseVFile createVFile(@Nonnull String name) throws CannotCreateFileException {
      return vfs.createVFile(this, name);
    }

    @Override
    @Nonnull
    public BaseVDir createVDir(@Nonnull String name) throws CannotCreateFileException {
      return vfs.createVDir(this, name);
    }

    @Override
    @Nonnull
    public Collection<? extends BaseVElement> list() {
      return vfs.list(this);
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

    @Override
    public void delete() throws CannotDeleteFileException {
      vfs.delete(this);
    }
  }

  @Nonnull
  private static final Splitter splitter = Splitter.on(ZipUtils.ZIP_SEPARATOR);
  @Nonnull
  private static final Set<Capabilities> CAPABILITIES = Collections.unmodifiableSet(
      EnumSet.of(Capabilities.READ, Capabilities.PARALLEL_READ, Capabilities.CASE_SENSITIVE));
  @Nonnull
  private final ZipVDir root = new ZipVDir(this, new ZipEntry(""), "");
  @Nonnull
  private final InputZipFile inputZipFile;
  @Nonnull
  private final ZipFile zipFile;
  @CheckForNull
  private String infoString;

  public ReadZipFS(@Nonnull InputZipFile zipFile) {
    this.inputZipFile = zipFile;
    this.zipFile = zipFile.getZipFile();
    loadSubElements();
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
  public synchronized void close() throws CannotCloseException {
    if (!closed) {
      try {
        zipFile.close();
      } catch (IOException e) {
        throw new CannotCloseException(this, e);
      }
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
      VFSStatCategory.ZIP_READ.getCounterStat(getTracer(), infoString).incValue();

      return zipFile.getInputStream(file.getZipEntry());
    } catch (IOException e) {
      // Only IOException actually thrown is when compression method is unknown
      throw new AssertionError(e);
    }
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull ZipVFile file) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull ZipVFile file, boolean append) {
    throw new UnsupportedOperationException();
  }

  //
  // VElement
  //

  @Override
  @Nonnull
  ZipVDir getVDir(@Nonnull ZipVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    BaseVElement element = parent.getFromCache(name);
    if (element != null) {
      if (element.isVDir()) {
        return (ZipVDir) element;
      } else {
        throw new NotDirectoryException(getVDirLocation(parent, name));
      }
    } else {
      throw new NoSuchFileException(getVDirLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  ZipVFile getVFile(@Nonnull ZipVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    BaseVElement element = parent.getFromCache(name);
    if (element != null) {
      if (!element.isVDir()) {
        return (ZipVFile) element;
      } else {
        throw new NotFileException(getVFileLocation(parent, name));
      }
    } else {
      throw new NoSuchFileException(getVFileLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  ZipVDir createVDir(@Nonnull ZipVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  ZipVFile createVFile(@Nonnull ZipVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  private synchronized ZipVDir loadVDir(@Nonnull ZipVDir parent, @Nonnull String name) {
    // synchronized to make "get" and "put" cache accesses atomic
    ZipVDir vDir = (ZipVDir) parent.getFromCache(name);
    if (vDir == null) {
      vDir = new ZipVDir(this, new ZipEntry(parent.getZipEntry().getName() + name + '/'), name);
      parent.putInCache(name, vDir);
    }
    return vDir;
  }

  @Nonnull
  private ZipVFile loadVFile(@Nonnull ZipVDir parent, @Nonnull String name) {
    ZipVFile vFile =
        new ZipVFile(this, zipFile.getEntry(parent.getZipEntry().getName() + name), name);
    parent.putInCache(name, vFile);
    return vFile;
  }

  @Override
  @Nonnull
  void delete(@Nonnull ZipVFile file) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull ZipVDir dir) {
    return dir.getAllFromCache();
  }

  @Override
  boolean isEmpty(@Nonnull ZipVDir dir) {
    return dir.isEmpty();
  }

  @Override
  @Nonnull
  FileTime getLastModified(@Nonnull ZipVFile file) throws CannotGetModificationTimeException {
    return inputZipFile.getLastModified();
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
    return CAPABILITIES;
  }

  private void loadSubElements() {

    for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        ZipVDir currentDir = getRootDir();
        Iterator<String> names = splitter.split(entryName).iterator();

        String simpleName = null;
        while (names.hasNext()) {
          simpleName = names.next();
          assert !simpleName.isEmpty();
          if (names.hasNext()) {
            // simpleName is a dir name
            currentDir = loadVDir(currentDir, simpleName);
          }
        }
        loadVFile(currentDir, simpleName);
      }
    }
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull ZipVDir parent, @Nonnull ZipVFile file) {
    String fileEntryPath = file.getZipEntry().getName();
    String parentEntryPath = parent.getZipEntry().getName();
    assert fileEntryPath.startsWith(parentEntryPath);
    String newPath = fileEntryPath.substring(parentEntryPath.length());
    return new VPath(newPath, '/');
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull ZipVFile file) {
    return getPathFromDir(root, file);
  }

  public void setInfoString(@CheckForNull String infoString) {
    this.infoString = infoString;
  }

  @Override
  public String getInfoString() {
    return infoString;
  }

  @Override
  public String toString() {
    return "rZipFS: " + getLocation().getDescription();
  }
}
