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

import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.WriteZipFS.ZipVDir;
import com.android.sched.vfs.WriteZipFS.ZipVFile;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} implementation backed by a zip archive that only supports writing.
 */
public class WriteZipFS extends BaseVFS<ZipVDir, ZipVFile> implements VFS {


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
  private final ZipVDir root = new ZipVDir(this, new ZipEntry(""), "");
  @Nonnull
  private final AtomicBoolean lastVFileOpen = new AtomicBoolean(false);
  @Nonnull
  private final OutputZipFile zipFile;
  @Nonnull
  private final ZipOutputStream outputStream;
  @Nonnull
  private final Set<Capabilities> capabilities;

  public WriteZipFS(@Nonnull OutputZipFile zipFile) {
    this.zipFile = zipFile;
    outputStream = zipFile.getOutputStream();

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    capabilities.add(Capabilities.WRITE);
    capabilities.add(Capabilities.CASE_SENSITIVE);
    this.capabilities = Collections.unmodifiableSet(capabilities);
  }

  void notifyVFileClosed() {
    boolean previousState = lastVFileOpen.getAndSet(false);
    assert previousState;
  }

  boolean notifyVFileOpenAndReturnPreviousState() {
    return lastVFileOpen.getAndSet(true);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "zip archive writer";
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return zipFile.getLocation();
  }

  @Override
  @Nonnull
  public String getPath() {
    return zipFile.getPath();
  }

  @Override
  @Nonnull
  public ZipVDir getRootDir() {
    return root;
  }

  @Override
  public synchronized void close() throws IOException {
    if (!closed) {
      outputStream.close();
      closed = true;
    }
  }

  //
  // Stream
  //

  @Override
  @Nonnull
  InputStream openRead(@Nonnull ZipVFile file) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  synchronized OutputStream openWrite(@Nonnull ZipVFile file) {
    assert !isClosed();

    try {
      outputStream.putNextEntry(file.getZipEntry());
      if (notifyVFileOpenAndReturnPreviousState()) {
        throw new AssertionError(getLocation().getDescription()
            + " cannot be written to because a previous stream has not been closed.");
      }
      return new UnclosableVFileOutputStream(this);
    } catch (IOException e) {
      // TODO(jplesot): Auto-generated catch block
      throw new AssertionError(e);
    }
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

    return new ZipVFile(this, new ZipEntry(parent.getZipEntry().getName() + name), name);
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
    throw new UnsupportedOperationException();
  }

  //
  // Location
  //

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVFile file) {
    return new ZipLocation(zipFile.getLocation(), file.getZipEntry());
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVDir parent, @Nonnull String name) {
    return new ZipLocation(zipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + name));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir dir) {
    return new ZipLocation(zipFile.getLocation(), dir.getZipEntry());
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir parent, @Nonnull String name) {
    return new ZipLocation(zipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + name + '/'));
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull ZipVDir parent, @Nonnull VPath path) {
    return new ZipLocation(zipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + path.getPathAsString('/')));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull ZipVDir parent, @Nonnull VPath path) {
    return new ZipLocation(zipFile.getLocation(),
        new ZipEntry(parent.getZipEntry().getName() + path.getPathAsString('/') + '/'));
  }

  //
  // Misc
  //

  @Override
  public boolean needsSequentialWriting() {
    return true;
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  private static class UnclosableVFileOutputStream extends FilterOutputStream {
    @Nonnull
    private final WriteZipFS vfs;

    public UnclosableVFileOutputStream(@Nonnull WriteZipFS vfs) {
      super(vfs.outputStream);
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
