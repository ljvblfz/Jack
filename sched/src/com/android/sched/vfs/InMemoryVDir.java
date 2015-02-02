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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A 'in-memory' implementation of a {@link VDir}. All sub-elements are cached in-memory for ever.
 *
 * When using this {@link VDir} implementation, a {@link BaseVFS} implementation has no need to
 * implement {@link BaseVFS#getVFile(BaseVDir, String)}, {@link BaseVFS#getVDir(BaseVDir, String)},
 * {@link BaseVFS#isEmpty(BaseVDir)} and {@link BaseVFS#list(BaseVDir)}.
 */
abstract class InMemoryVDir extends BaseVDir {
  @Nonnull
  private final Map<String, BaseVElement> map = new HashMap<String, BaseVElement>();

  public InMemoryVDir(@Nonnull BaseVFS<? extends InMemoryVDir, ? extends BaseVFile> vfs,
      @Nonnull String name) {
    super(vfs, name);
  }

  @Override
  @Nonnull
  public synchronized BaseVDir getVDir(@Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    assert !vfs.isClosed();

    BaseVElement element = map.get(name);
    if (element != null) {
      if (element.isVDir()) {
        return (BaseVDir) element;
      } else {
        throw new NotDirectoryException(vfs.getVDirLocation(this, name));
      }
    } else {
      throw new NoSuchFileException(vfs.getVDirLocation(this, name));
    }
  }

  @Override
  @Nonnull
  public synchronized BaseVFile getVFile(@Nonnull String name) throws NoSuchFileException,
      NotFileException {
    assert !vfs.isClosed();

    BaseVElement element = map.get(name);
    if (element != null) {
      if (!element.isVDir()) {
        return (BaseVFile) element;
      } else {
        throw new NotFileException(vfs.getVFileLocation(this, name));
      }
    } else {
      throw new NoSuchFileException(vfs.getVFileLocation(this, name));
    }
  }

  @Override
  @Nonnull
  public synchronized BaseVDir createVDir(@Nonnull String name) throws CannotCreateFileException {
    try {
      return getVDir(name);
    } catch (NoSuchFileException e) {
      BaseVDir dir = vfs.createVDir(this, name);
      map.put(name, dir);

      return dir;
    } catch (NotDirectoryException e) {
      throw new CannotCreateFileException(vfs.getVDirLocation(this, name));
    }
  }

  @Override
  @Nonnull
  public synchronized BaseVFile createVFile(@Nonnull String name) throws CannotCreateFileException {
    try {
      return getVFile(name);
    } catch (NoSuchFileException e) {
      BaseVFile file = vfs.createVFile(this, name);
      map.put(name, file);

      return file;
    } catch (NotFileException e) {
      throw new CannotCreateFileException(vfs.getVFileLocation(this, name));
    }
  }

  @Override
  @Nonnull
  public synchronized Collection<? extends BaseVElement> list() {
//    assert !vfs.isClosed();
    // STOPSHIP: There is a problem with this assertion, likely due to PrefixedFS that has a close()
    // method that is not related to the the close() method of the underlying VFS.

    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  synchronized void internalDelete(@Nonnull String name) {
    map.remove(name);
  }

  @CheckForNull
  synchronized BaseVElement getFromCache(@Nonnull String name) {
    return map.get(name);
  }

  synchronized void putInCache(@Nonnull String name, @Nonnull BaseVElement vElement) {
    map.put(name, vElement);
  }

  synchronized Collection<? extends BaseVElement> getAllFromCache() {
    return map.values();
  }
}

