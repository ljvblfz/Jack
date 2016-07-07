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
import com.android.sched.util.location.Location;
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

  @Nonnull
  protected List<Location> locationList = new ArrayList<Location>(1);

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
  public VFS getVfs() {
    return vfs;
  }

  @Override
  public boolean canBeMerged(@Nonnull List<? extends InputLibrary> inputLibraries) {

    if (this instanceof InputJackLibrary && !((InputJackLibrary) this).hasCompliantPrebuilts()) {
      return false;
    }

    int currentMajorVersion = getMajorVersion();
    for (InputLibrary inputLib : inputLibraries) {
      if (!(inputLib instanceof InputJackLibrary)
          || !(inputLib.getMajorVersion() == currentMajorVersion)
          || !((InputJackLibrary) inputLib).containsFileType(FileType.PREBUILT)
          || !((InputJackLibrary) inputLib).hasCompliantPrebuilts()
          || ((InputJackLibrary) inputLib).hasJayceDigest()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void mergeInputLibraries(
      @Nonnull List<? extends InputLibrary> inputLibraries) {
    // merge can only be done before the VFSes are accessed
    assert locationList.size() == 1;

    assert canBeMerged(inputLibraries);

    List<VFS> inputLibVfsList = new ArrayList<VFS>(inputLibraries.size());
    for (InputLibrary inputLib : inputLibraries) {
      inputLibVfsList.add(((InputJackLibrary) inputLib).getVfs());
      locationList.add(inputLib.getLocation());
      fileTypes.addAll(((InputJackLibrary) inputLib).getFileTypes());
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

  public boolean containsLibraryLocation(@Nonnull Location location) {
    return locationList.contains(location);
  }

  public boolean hasJayceDigest() {
    return libraryProperties.getProperty(JackLibrary.KEY_LIB_JAYCE_DIGEST, "").equals("true");
  }
}
