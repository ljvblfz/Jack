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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.UnionVFS.UnionVDir;
import com.android.sched.vfs.UnionVFS.UnionVFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} that acts as an ordered agglomerate of other VFS. Writing is done in the top VFS
 * only, if it is supported. The VFS are ordered according to their priority.
 */
public class UnionVFS extends BaseVFS<UnionVDir, UnionVFile> implements VFS {

  static class UnionVFile extends ParentVFile {

    @Nonnull
    private BaseVFile wrappedFile;

    private boolean writable;

    public UnionVFile(@Nonnull BaseVFS<UnionVDir, UnionVFile> vfs,
        @Nonnull UnionVDir parent, @Nonnull BaseVFile wrappedFile, boolean writable) {
      super(vfs, parent, wrappedFile.getName());
      this.wrappedFile = wrappedFile;
      this.writable = writable;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return wrappedFile.getPath();
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return wrappedFile.getLocation();
    }

    @Nonnull
    BaseVFile getWrappedFile() {
      return wrappedFile;
    }

    boolean isWritable() {
      return writable;
    }

    synchronized void setWritableWrappedFile(BaseVFile writableFile) {
      wrappedFile = writableFile;
      writable = true;
    }
  }

  static class UnionVDir extends ParentVDir {

    boolean fullyLoaded = false;

    private boolean writable;

    @Nonnull
    private final List<BaseVDir> wrappedDirs;

    public UnionVDir(@Nonnull BaseVFS<UnionVDir, UnionVFile> vfs,
        @Nonnull List<BaseVDir> wrappedDirs, boolean writable) {
      super(vfs, wrappedDirs.get(0).getName());
      this.wrappedDirs = Collections.synchronizedList(wrappedDirs);
      this.writable = writable;
    }

    public UnionVDir(@Nonnull BaseVFS<UnionVDir, UnionVFile> vfs,
        @Nonnull UnionVDir parent, @Nonnull List<BaseVDir> wrappedDirs, boolean writable) {
      super(vfs, parent, wrappedDirs.get(0).getName());
      this.wrappedDirs = Collections.synchronizedList(wrappedDirs);
      this.writable = writable;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return wrappedDirs.get(0).getPath();
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return wrappedDirs.get(0).getLocation();
    }

    /**
     * Iteration on the returned list must be synchronized.
     */
    @Nonnull
    List<BaseVDir> getWrappedDirs() {
      return wrappedDirs;
    }

    boolean isWritable() {
      return writable;
    }

    synchronized void addWritableWrappedDir(@Nonnull BaseVDir writableDir){
      wrappedDirs.add(0, writableDir);
      writable = true;
    }

    synchronized void ensureFullyLoaded() {
      // synchronized for the atomicity of modifications to "wrappedDirs"
      if (!fullyLoaded) {
        UnionVDir parent = (UnionVDir) getParent();
        if (parent != null) {
          parent.ensureFullyLoaded();

          List<BaseVDir> parentWrappedDirs = parent.getWrappedDirs();
          synchronized (parentWrappedDirs) { // iteration needs to be synchronized
            for (BaseVDir parentWrappedDir : parentWrappedDirs) {
              // check if the wrappedDir corresponding to this parentWrappedDir is already contained
              boolean alreadyContained = false;
              synchronized (wrappedDirs) { // iteration needs to be synchronized
                for (BaseVDir wrappedDir : wrappedDirs) {
                  if (wrappedDir.getVFS() == parentWrappedDir.getVFS()) {
                    alreadyContained = true;
                    break;
                  }
                }
              }
              if (!alreadyContained) {
                try {
                  BaseVDir newWrappedDir = parentWrappedDir.getVDir(name);
                  wrappedDirs.add(newWrappedDir);
                } catch (NotDirectoryException e) {
                  throw new AssertionError(e);
                } catch (NoSuchFileException e) {
                  // ignore
                }
              }
            }
          }
        }

        fullyLoaded = true;
      }
    }

    void internalDelete(@Nonnull String name) throws CannotDeleteFileException {
      ensureFullyLoaded();
      synchronized (wrappedDirs) { // iteration needs to be synchronized
        for (BaseVDir wrappedDir : wrappedDirs) {
          try {
            BaseVFile vFile = wrappedDir.getVFile(name);
            wrappedDir.delete(vFile);
          } catch (NotFileException e) {
            // ignore
          } catch (NoSuchFileException e) {
            // ignore
          }
        }
      }
    }
  }

  @Nonnull
  List<VFS> vfsList;
  @Nonnull
  private final Set<Capabilities> capabilities;

  @Nonnull
  private final UnionVDir rootDir;

  private final boolean writable;

  public UnionVFS(@Nonnull List<VFS> vfsList) {
    assert !vfsList.isEmpty();
    this.vfsList = vfsList;
    List<BaseVDir> wrappedDirs = new ArrayList<BaseVDir>(vfsList.size());
    for (VFS vfs : vfsList) {
      wrappedDirs.add((BaseVDir) vfs.getRootDir());
    }

    VFS topVfs = vfsList.get(0);

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    for (Capabilities topVfsCapability : topVfs.getCapabilities()) {
      switch (topVfsCapability) {
        case CASE_SENSITIVE:
          if (isSupportedByAll(topVfsCapability)) {
            capabilities.add(topVfsCapability);
          }
          break;
        case DIGEST:
          if (isSupportedByAll(topVfsCapability)) {
            capabilities.add(topVfsCapability);
          }
          break;
        case PARALLEL_READ:
          if (isSupportedByAll(topVfsCapability)) {
            capabilities.add(topVfsCapability);
          }
          break;
        case PARALLEL_WRITE:
          capabilities.add(topVfsCapability);
          break;
        case READ:
          if (isSupportedByAny(topVfsCapability)) {
            capabilities.add(topVfsCapability);
          }
          break;
        case UNIQUE_ELEMENT:
          // not supported by UnionVFS
          break;
        case WRITE:
          capabilities.add(topVfsCapability);
          break;
        default:
          throw new AssertionError();
      }
    }
    this.capabilities = Collections.unmodifiableSet(capabilities);

    writable = topVfs.getCapabilities().contains(Capabilities.WRITE);
    this.rootDir = new UnionVDir(this, wrappedDirs, writable);
  }

  private boolean isSupportedByAll(@Nonnull Capabilities capability) {
    boolean supportedByAll = true;
    for (VFS vfs : vfsList) {
      if (!vfs.getCapabilities().contains(capability)) {
        supportedByAll = false;
        break;
      }
    }
    return supportedByAll;
  }

  private boolean isSupportedByAny(@Nonnull Capabilities capability) {
    boolean supportedByAny = false;
    for (VFS vfs : vfsList) {
      if (vfs.getCapabilities().contains(capability)) {
        supportedByAny = true;
        break;
      }
    }
    return supportedByAny;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfsList.get(0).getLocation();
  }

  @Override
  public void close() throws CannotCloseException {
    if (!closed) {
      for (VFS vfs : vfsList) {
        vfs.close();
      }
      closed = true;
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    StringBuilder sb = new StringBuilder("a union between \"");
    Joiner joiner = Joiner.on("\", \"");
    List<String> descriptionList = Lists.transform(vfsList, new Function<VFS, String>() {

      @Override
      public String apply(VFS vfs) {
        return vfs.getDescription();
      }});
    joiner.appendTo(sb, descriptionList);
    return sb.append("\"").toString();
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfsList.get(0).getPath();
  }

  public boolean isWritable() {
    return writable;
  }

  @Override
  public boolean needsSequentialWriting() {
    return !capabilities.contains(Capabilities.PARALLEL_WRITE);
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  @Override
  @Nonnull
  public UnionVDir getRootDir() {
    return rootDir;
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull UnionVFile file) throws WrongPermissionException {
    return file.getWrappedFile().getInputStream();
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull UnionVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull UnionVFile file, boolean append) throws WrongPermissionException {
    if (!isWritable()) {
      throw new UnsupportedOperationException();
    }

    synchronized (file) {
      if (!file.isWritable()) {
        try {
          loadWritableFile(file);
        } catch (CannotCreateFileException e) {
          throw new AssertionError(e);
        }
      }
    }

    return file.getWrappedFile().getOutputStream(append);
  }

  private void loadWritableFile(@Nonnull UnionVFile file) throws CannotCreateFileException {
    assert isWritable();
    UnionVDir parent = (UnionVDir) file.getParent();
    synchronized (parent) {
      if (!parent.isWritable()) {
        loadWritableDir(parent);
      }
    }
    file.setWritableWrappedFile(parent.getWrappedDirs().get(0).createVFile(file.getName()));
  }

  private void loadWritableDir(@Nonnull UnionVDir dir) throws CannotCreateFileException {
    assert isWritable();
    UnionVDir parent = (UnionVDir) dir.getParent();
    assert parent != null;
    synchronized (parent) {
      if (!parent.isWritable()) {
        loadWritableDir(parent);
      }
    }
    dir.addWritableWrappedDir(parent.getWrappedDirs().get(0).createVDir(dir.getName()));
  }

  @Override
  @Nonnull
  UnionVDir getVDir(@Nonnull UnionVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    parent.ensureFullyLoaded();
    List<BaseVDir> parentWrappedDirs = parent.getWrappedDirs();
    BaseVDir dirToWrap = null;
    boolean writable = parent.isWritable();
    synchronized (parentWrappedDirs) { // iteration needs to be synchronized
      for (BaseVDir parentWrappedDir : parentWrappedDirs) {
        try {
          dirToWrap = parentWrappedDir.getVDir(name);
          break; // break only if no exception
        } catch (NoSuchFileException e) {
          // ignore and try next
          writable = false; // only the first wrappedDir may be writable
        }
      }
    }
    if (dirToWrap == null) {
      throw new NoSuchFileException(getVDirLocation(parent, name));
    }
    return new UnionVDir(this, parent, Lists.newArrayList(dirToWrap), writable);
  }

  @Override
  @Nonnull
  UnionVFile getVFile(@Nonnull UnionVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    parent.ensureFullyLoaded();
    List<BaseVDir> parentWrappedDirs = parent.getWrappedDirs();
    BaseVFile fileToWrap = null;
    boolean writable = parent.isWritable();
    synchronized (parentWrappedDirs) { // iteration needs to be synchronized
      for (BaseVDir parentWrappedDir : parentWrappedDirs) {
        try {
          fileToWrap = parentWrappedDir.getVFile(name);
          break; // break only if no exception
        } catch (NoSuchFileException e) {
          // ignore and try next
          writable = false; // only the first wrappedFile may be writable
        }
      }
    }
    if (fileToWrap == null) {
      throw new NoSuchFileException(getVFileLocation(parent, name));
    }
    return new UnionVFile(this, parent, fileToWrap, writable);
  }

  @Override
  @Nonnull
  UnionVDir createVDir(@Nonnull UnionVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    UnionVDir vDir = null;
    try {
      vDir = getVDir(parent, name);
    } catch (NotDirectoryException e) {
      throw new AssertionError(e);
    } catch (NoSuchFileException e) {
      // ignore
    }

    if (vDir == null) {
      if (!isWritable()) {
        throw new UnsupportedOperationException();
      }

      synchronized (parent) {
        if (!parent.isWritable()) {
          loadWritableDir(parent);
        }
      }
      BaseVDir dirToWrap = parent.getWrappedDirs().get(0).createVDir(name);
      vDir = new UnionVDir(this, parent, Lists.newArrayList(dirToWrap), true /* = writable */);
    }

    return vDir;
  }

  @Override
  @Nonnull
  UnionVFile createVFile(@Nonnull UnionVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    UnionVFile vFile = null;
    try {
      vFile = getVFile(parent, name);
    } catch (NotFileException e) {
      throw new AssertionError(e);
    } catch (NoSuchFileException e) {
      // ignore
    }

    if (vFile == null) {
      if (!isWritable()) {
        throw new UnsupportedOperationException();
      }

      synchronized (parent) {
        if (!parent.isWritable()) {
          loadWritableDir(parent);
        }
      }
      BaseVFile fileToWrap = parent.getWrappedDirs().get(0).createVFile(name);
      vFile = new UnionVFile(this, parent, fileToWrap, true /* = writable */);
    }

    return vFile;
  }

  @Override
  @Nonnull
  void delete(@Nonnull UnionVFile file) throws CannotDeleteFileException {
    if (!isWritable()) {
      throw new UnsupportedOperationException();
    }

    try {
      UnionVDir parent = (UnionVDir) file.getParent();
      parent.internalDelete(file.getName());
    } catch (UnsupportedOperationException e) {
      // deleting from an underlying zip may throw an UnsupportedOperationException. We could
      // support it in the future, but for now we chose not too.
      throw new UnionVFSReadOnlyException(e);
    }
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull UnionVDir dir) {
    dir.ensureFullyLoaded();
    List<BaseVElement> unionElements = new ArrayList<BaseVElement>();
    List<BaseVDir> wrappedDirs = dir.getWrappedDirs();
    synchronized (wrappedDirs) { // iteration needs to be synchronized
      for (BaseVDir wrappedDir : wrappedDirs) {
        boolean writable = dir.isWritable();
        for (BaseVElement subWrappedElement : wrappedDir.list()) {
          String currentName = subWrappedElement.getName();

          // check if UnionVElement already exists
          boolean unionVElementExists = false;
          for (BaseVElement unionVElement : unionElements) {
            if (unionVElement.getName().equals(currentName)) {
              unionVElementExists = true;
              break;
            }
          }

          if (!unionVElementExists) { // it does not exist, create it
            BaseVElement unionElement;
            if (subWrappedElement.isVDir()) {
              unionElement = new UnionVDir(this, dir,
                  Lists.newArrayList((BaseVDir) subWrappedElement), writable);
            } else {
              unionElement = new UnionVFile(this, dir, (BaseVFile) subWrappedElement, writable);
            }
            unionElements.add(unionElement);
          }
          writable = false; // only the top subWrappedElement can be writable
        }
      }

    }

    return unionElements;
  }

  @Override
  boolean isEmpty(@Nonnull UnionVDir dir) {
    return list(dir).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull UnionVDir parent, @Nonnull UnionVFile file) {
    BaseVFile wrappedFile = file.getWrappedFile();
    VFS fileVFS = wrappedFile.getVFS();
    BaseVDir matchingDir = null;
    List<BaseVDir> parentWrappedDirs = parent.getWrappedDirs();
    synchronized (parentWrappedDirs) { // iteration needs to be synchronized
      for (BaseVDir parentWrappedDir : parentWrappedDirs) {
        if (parentWrappedDir.getVFS() == fileVFS) {
          matchingDir = parentWrappedDir;
          break;
        }
      }
    }
    assert matchingDir != null;
    return ((BaseVFS<BaseVDir, BaseVFile>) fileVFS).getPathFromDir(matchingDir, wrappedFile);
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull UnionVFile file) {
    return file.getWrappedFile().getPathFromRoot();
  }

  @Override
  long getLastModified(@Nonnull UnionVFile file) {
    return file.getWrappedFile().getLastModified();
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull UnionVFile file) {
    return file.getWrappedFile().getLocation();
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull UnionVDir parent, @Nonnull String name) {
    BaseVDir parentWrappedDir = parent.getWrappedDirs().get(0);
    return parentWrappedDir.getVFS().getVFileLocation(parentWrappedDir, name);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull UnionVDir parent, @Nonnull VPath path) {
    BaseVDir parentWrappedDir = parent.getWrappedDirs().get(0);
    return parentWrappedDir.getVFS().getVFileLocation(parentWrappedDir, path);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull UnionVDir dir) {
    return dir.getWrappedDirs().get(0).getLocation();
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull UnionVDir parent, @Nonnull String name) {
    BaseVDir parentWrappedDir = parent.getWrappedDirs().get(0);
    return parentWrappedDir.getVFS().getVDirLocation(parentWrappedDir, name);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull UnionVDir parent, @Nonnull VPath path) {
    BaseVDir parentWrappedDir = parent.getWrappedDirs().get(0);
    return parentWrappedDir.getVFS().getVDirLocation(parentWrappedDir, path);
  }

  @Override
  public String toString() {
    return "unionFS >> " + vfsList.toString();
  }

}
