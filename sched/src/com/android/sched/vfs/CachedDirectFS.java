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

import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.QueryableInputStream;
import com.android.sched.util.stream.QueryableOutputStream;
import com.android.sched.util.stream.QueryableStream;
import com.android.sched.vfs.CachedDirectFS.CachedParentVDir;
import com.android.sched.vfs.CachedDirectFS.CachedParentVFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link VFS} implementation backed by a real file system, but where directories are cached in
 * memory.
 */
public class CachedDirectFS extends BaseVFS<CachedParentVDir, CachedParentVFile> implements VFS {

  static class CachedParentVDir extends InMemoryVDir {

    @CheckForNull
    private CachedParentVDir parent;

    CachedParentVDir(@Nonnull BaseVFS<? extends CachedParentVDir, ? extends ParentVFile> vfs,
        @Nonnull String name) {
      super(vfs, name);
    }

    CachedParentVDir(@Nonnull BaseVFS<? extends CachedParentVDir, ? extends ParentVFile> vfs,
        @Nonnull CachedParentVDir parent, @Nonnull String name) {
      super(vfs, name);
      this.parent = parent;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      if (parent != null) {
        return parent.getPath().clone().appendPath(new VPath(name, '/'));
      } else {
        return VPath.ROOT;
      }
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

    @CheckForNull
    public CachedParentVDir getParent() {
      return parent;
    }
  }

  static class CachedParentVFile extends ParentVFile {

    CachedParentVFile(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs,
        @Nonnull VDir parent, @Nonnull String name) {
      super(vfs, parent, name);
    }

    @Override
    public void delete() throws CannotDeleteFileException {
      vfs.delete(this);
    }

    public void deleteFromCache() {
      ((InMemoryVDir) parent).internalDelete(name);
    }
  }

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Directory  dir;
  @Nonnull
  private final CachedParentVDir root;
  @Nonnull
  private final Set<Capabilities> capabilities;
  @Nonnull
  private final List<TrackedStream> openedStreams =
      Collections.synchronizedList(new LinkedList<TrackedStream>());
  @CheckForNull
  private String infoString;


  public CachedDirectFS(@Nonnull Directory dir, int permissions) {
    this.dir = dir;
    this.root = new CachedParentVDir(this, "");

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    if ((permissions & Permission.READ) != 0) {
      capabilities.add(Capabilities.READ);
      capabilities.add(Capabilities.PARALLEL_READ);
    }
    if ((permissions & Permission.WRITE) != 0) {
      capabilities.add(Capabilities.WRITE);
      capabilities.add(Capabilities.PARALLEL_WRITE);
    }
    capabilities.add(Capabilities.UNIQUE_ELEMENT);
    this.capabilities = Collections.unmodifiableSet(capabilities);

    fillVDirFromRealDirectory(dir.getFile(), root);
  }

  private void fillVDirFromRealDirectory(@Nonnull File dir, @Nonnull VDir vDir) {
    File[] fileList = dir.listFiles();
    assert fileList != null;
    for (File element : fileList) {
      try {
        if (element.isDirectory()) {
          VDir newVDir = vDir.createVDir(element.getName());
          fillVDirFromRealDirectory(element, newVDir);
        } else {
          vDir.createVFile(element.getName());
        }
      } catch (CannotCreateFileException e) {
        throw new AssertionError(e);
      }
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "directory on disk with cache";
  }

  @Nonnull
  @Override
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return dir.getLocation();
  }

  @Override
  public synchronized void close() {
    if (!closed) {
      assert areStreamsClosed()
          : "At least one stream from VFS '"
              + getDescription()
              + "' in "
              + getLocation().getDescription()
              + " has not been closed, see log for details";
      openedStreams.clear();
      closed = true;
    }
  }

  private boolean areStreamsClosed() {
    boolean allClosed = true;
    synchronized (openedStreams) { // iteration on synchronized list needs to use the list as lock
      for (TrackedStream stream : openedStreams) {
        if (!stream.isClosed()) {
          logger.log(
              Level.SEVERE,
              "Stream from {0} hasn't been closed",
              stream.getLocation().getDescription());
          allClosed &= false;
        }
      }
    }
    return allClosed;
  }

  @Override
  @Nonnull
  public String getPath() {
    return dir.getPath();
  }

  @Override
  public CachedParentVDir getRootDir() {
    return root;
  }

  @SuppressWarnings("resource")
  @Override
  @Nonnull
  InputStream openRead(@Nonnull CachedParentVFile file) throws WrongPermissionException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    VFSStatCategory.DIR_READ.getCounterStat(getTracer(), infoString).incValue();

    File path = getNativeFile(file.getPath());
    try {
      InputStream is = new FileInputStream(path);
      assert (is = trackOpenedStream(is, file)) != null;
      return is;
    } catch (FileNotFoundException e) {

      DirectFS.checkPermissionsIfExists(path, file.getLocation(), Permission.READ);

      // should not happen
      throw new AssertionError(e);
    }
  }

  @Nonnull
  @Override
  OutputStream openWrite(@Nonnull CachedParentVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @SuppressWarnings("resource")
  @Nonnull
  @Override
  OutputStream openWrite(@Nonnull CachedParentVFile file, boolean append)
      throws WrongPermissionException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.WRITE);

    VFSStatCategory.DIR_WRITE.getCounterStat(getTracer(), infoString).incValue();

    File path = getNativeFile(file.getPath());
    try {
      OutputStream os = new FileOutputStream(path, append);
      assert (os = trackOpenedStream(os, file)) != null;
      return os;
    } catch (FileNotFoundException e) {

      DirectFS.checkPermissionsIfExists(path, file.getLocation(), Permission.WRITE);

      // should not happen
      throw new AssertionError(e);
    }
  }

  private interface TrackedStream extends QueryableStream, HasLocation {
  }

  private static class TrackedInputStream extends QueryableInputStream implements TrackedStream {

    private final VFile file;

    public TrackedInputStream(@Nonnull InputStream is, @Nonnull VFile file) {
      super(is);
      this.file = file;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return file.getLocation();
    }
  }

  private static class TrackedOutputStream extends QueryableOutputStream implements TrackedStream {

    private final VFile file;

    public TrackedOutputStream(@Nonnull OutputStream os, @Nonnull VFile file) {
      super(os);
      this.file = file;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return file.getLocation();
    }
  }

  @Nonnull
  private InputStream trackOpenedStream(@Nonnull InputStream is, @Nonnull VFile file) {
    TrackedInputStream qis = new TrackedInputStream(is, file);
    openedStreams.add(qis);
    return qis;
  }

  @Nonnull
  private OutputStream trackOpenedStream(@Nonnull OutputStream os, @Nonnull VFile file) {
    TrackedOutputStream qos = new TrackedOutputStream(os, file);
    openedStreams.add(qos);
    return qos;
  }

  @Nonnull
  @Override
  Collection<? extends BaseVElement> list(@Nonnull CachedParentVDir dir) {
    return dir.getAllFromCache();
  }

  @Override
  boolean isEmpty(@Nonnull CachedParentVDir dir) {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    File[] fileList = getNativeFile(dir.getPath()).listFiles();
    assert fileList != null;
    return fileList.length == 0;
  }

  @Override
  @Nonnull
  CachedParentVDir getVDir(@Nonnull CachedParentVDir parent, @Nonnull String name)
      throws NotDirectoryException, NoSuchFileException {
    BaseVElement element = parent.getFromCache(name);
    if (element != null) {
      if (element.isVDir()) {
        return (CachedParentVDir) element;
      } else {
        throw new NotDirectoryException(getVDirLocation(parent, name));
      }
    } else {
      throw new NoSuchFileException(getVDirLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  CachedParentVFile getVFile(@Nonnull CachedParentVDir parent, @Nonnull String name)
      throws NotFileException, NoSuchFileException {
    BaseVElement element = parent.getFromCache(name);
    if (element != null) {
      if (!element.isVDir()) {
        return (CachedParentVFile) element;
      } else {
        throw new NotFileException(getVFileLocation(parent, name));
      }
    } else {
      throw new NoSuchFileException(getVFileLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  void delete(@Nonnull CachedParentVFile file) throws CannotDeleteFileException {
    assert !isClosed();

    File path = getNativeFile(file.getPath());
    if (!path.delete() || path.exists()) {
      throw new CannotDeleteFileException(file);
    }

    file.deleteFromCache();
  }


  @Override
  @Nonnull
  synchronized CachedParentVFile createVFile(@Nonnull CachedParentVDir parent,
      @Nonnull String name) throws CannotCreateFileException {
    assert !isClosed();

    try {
      return getVFile(parent, name);

    } catch (NoSuchFileException e) {

      File path = getNativeFile(parent.getPath(), name);
      try {
        AbstractStreamFile.create(path, new FileLocation(path));

        VFSStatCategory.DIR_CREATE.getPercentStat(getTracer(), infoString).addTrue();
      } catch (FileAlreadyExistsException e2) {
        // Nothing to do
        VFSStatCategory.DIR_CREATE.getPercentStat(getTracer(), infoString).addFalse();
      }
    CachedParentVFile vFile = new CachedParentVFile(this, parent, name);

    parent.putInCache(name, vFile);
    return vFile;

    } catch (NotFileException e) {
      throw new CannotCreateFileException(getVFileLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  synchronized CachedParentVDir createVDir(@Nonnull CachedParentVDir parent,
      @Nonnull String name)
      throws CannotCreateFileException {
    assert !isClosed();

    try {
      return getVDir(parent, name);

    } catch (NoSuchFileException e) {

      File path = getNativeFile(parent.getPath(), name);
      try {
        Directory.create(path, new DirectoryLocation(path));
      } catch (FileAlreadyExistsException e2) {
        // Nothing to do
      }
      CachedParentVDir vDir = new CachedParentVDir(this, parent, name);

      parent.putInCache(name, vDir);
      return vDir;

    } catch (NotDirectoryException e) {
      throw new CannotCreateFileException(getVDirLocation(parent, name));
    }
  }

  @Override
  public boolean needsSequentialWriting() {
    return false;
  }

  @Override
  @Nonnull
  public FileTime getLastModified(@Nonnull CachedParentVFile file)
      throws CannotGetModificationTimeException {
    try {
      return Files.getLastModifiedTime(getNativeFile(file.getPath()).toPath());
    } catch (IOException e) {
      throw new CannotGetModificationTimeException(this, e);
    }
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(@Nonnull CachedParentVFile file) {
    return new FileLocation(getNativeFile(file.getPath()));
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(@Nonnull CachedParentVDir parent, @Nonnull String name) {
    return new FileLocation(getNativeFile(parent.getPath(), name));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(@Nonnull CachedParentVDir dir) {
    return new DirectoryLocation(getNativeFile(dir.getPath()));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(@Nonnull CachedParentVDir parent, @Nonnull String name) {
    return new DirectoryLocation(getNativeFile(parent.getPath(), name));
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(CachedParentVDir parent, VPath path) {
    return new FileLocation(getNativeFile(parent.getPath().clone().appendPath(path)));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(CachedParentVDir parent, VPath path) {
    return new DirectoryLocation(getNativeFile(parent.getPath().clone().appendPath(path)));
  }

  @Nonnull
  private File getNativeFile(@Nonnull VPath path) {
    return new File(dir.getFile(), path.getPathAsString(File.separatorChar));
  }

  @Nonnull
  private File getNativeFile(@Nonnull VPath path, @Nonnull String name) {
    return new File(new File(dir.getFile(), path.getPathAsString(File.separatorChar)), name);
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull CachedParentVDir parent, @Nonnull CachedParentVFile file) {
    StringBuilder path = getPathFromDirInternal(parent, (CachedParentVDir) file.getParent())
        .append(file.getName());
    return new VPath(path.toString(), '/');
  }

  @Nonnull
  private static StringBuilder getPathFromDirInternal(@Nonnull CachedParentVDir baseDir,
      @Nonnull CachedParentVDir currentDir) {
    if (baseDir == currentDir) {
      return new StringBuilder();
    }
    CachedParentVDir currentParent = currentDir.getParent();
    assert currentParent != null;
    return getPathFromDirInternal(baseDir, currentParent).append(currentDir.getName()).append('/');
  }

  @Override
  @Nonnull
  public VPath getPathFromRoot(@Nonnull CachedParentVFile file) {
    return getPathFromDir(root, file);
  }

  @Override
  @CheckForNull
  public String getInfoString() {
    return infoString;
  }

  public void setInfoString(@CheckForNull String infoString) {
    this.infoString = infoString;
  }

  @Override
  public String toString() {
    return "cachedDirFS: " + getLocation().getDescription();
  }
}
