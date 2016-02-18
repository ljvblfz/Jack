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

package com.android.jack.library;

import com.android.jack.Jack;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.ReadWriteZipFS;
import com.android.sched.vfs.UnionVFS;
import com.android.sched.vfs.VFS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Common part of {@link InputLibrary} and {@link OutputLibrary}
 */
public abstract class CommonJackLibrary implements JackLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  protected VFS vfs;

  @Nonnull
  protected final Properties libraryProperties;

  @Nonnull
  public final String keyJayceMajorVersion =
    buildPropertyName(FileType.JAYCE, ".version.major");
  @Nonnull
  public final String keyJayceMinorVersion =
    buildPropertyName(FileType.JAYCE, ".version.minor");

  // TODO(jack-team): Change it to protected
  @Nonnull
  public final Set<FileType> fileTypes = EnumSet.noneOf(FileType.class);

  public CommonJackLibrary(@Nonnull Properties libraryProperties, @Nonnull VFS vfs) {
    this.libraryProperties = libraryProperties;
    this.vfs = vfs;
  }

  @Override
  @Nonnull
  public boolean containsProperty(@Nonnull String key) {
    return libraryProperties.containsKey(key);
  }

  @Nonnull
  @Override
  public String getProperty(@Nonnull String key) throws MissingLibraryPropertyException {
    if (!libraryProperties.containsKey(key)) {
      throw new MissingLibraryPropertyException(key, getLocation());
    }
    return (String) libraryProperties.get(key);
  }

  public void putProperty(@Nonnull String key, @Nonnull String value) {
    libraryProperties.put(key, value);
  }

  @Nonnull
  public Collection<FileType> getFileTypes() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(fileTypes);
  }

  public boolean containsFileType(@Nonnull FileType fileType) {
    return fileTypes.contains(fileType);
  }

  @Override
  @Nonnull
  public String buildPropertyName(@Nonnull FileType type, @CheckForNull String suffix) {
    return (getPropertyPrefix(type) + (suffix == null ? "" : suffix));
  }

  @Nonnull
  protected abstract String getPropertyPrefix(@Nonnull FileType type);

  protected void addFileType(@Nonnull FileType ft) {
    fileTypes.add(ft);
  }

  protected void fillFileTypes() {
    for (FileType ft : FileType.values()) {
      try {
        String propertyName = buildPropertyName(ft, null /*suffix*/);
        if (containsProperty(propertyName) && Boolean.parseBoolean(getProperty(propertyName))) {
          fileTypes.add(ft);
        }
      } catch (MissingLibraryPropertyException e) {
        // should not happen since we checked that the property was contained
        throw new AssertionError(e);
      }
    }
  }

  protected void fillFiles(@Nonnull InputVDir vDir, @Nonnull List<InputVFile> files) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillFiles((InputVDir) subFile, files);
      } else {
        files.add((InputVFile) subFile);
      }
    }
  }

  @Nonnull
  protected VFS getVfs() {
    return vfs;
  }

  @Override
  public void mergeInputLibraries(@Nonnull List<? extends InputJackLibrary> inputLibraries) {
    // merge can only be done before the VFSes are accessed

    List<VFS> inputLibVfsList = new ArrayList<VFS>();
    for (InputLibrary inputLib : inputLibraries) {
      inputLibVfsList.add(((CommonJackLibrary) inputLib).getVfs());
    }

    if (vfs instanceof ReadWriteZipFS) {
      ReadWriteZipFS zipVFS = (ReadWriteZipFS) vfs;
      VFS previousWorkVfs = zipVFS.getWorkVFS();
      inputLibVfsList.add(0, previousWorkVfs);
      zipVFS.setWorkVFS(new UnionVFS(inputLibVfsList));
    } else {
      inputLibVfsList.add(0, vfs);
      vfs = new UnionVFS(inputLibVfsList);
    }
  }
}
