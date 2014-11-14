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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputLibrary} and {@link OutputLibrary}
 */
public abstract class CommonJackLibrary implements JackLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  protected final Properties libraryProperties;

  // TODO(jack-team): Change it to private
  @Nonnull
  public final Set<FileType> fileTypes = new HashSet<FileType>(FileType.values().length);

  public CommonJackLibrary(@Nonnull Properties libraryProperties) {
    this.libraryProperties = libraryProperties;
  }

  @Override
  @Nonnull
  public boolean containsProperty(@Nonnull String key) {
    return libraryProperties.containsKey(key);
  }

  @Nonnull
  @Override
  public String getProperty(@Nonnull String key) throws LibraryFormatException {
    if (!libraryProperties.containsKey(key)) {
      logger.log(Level.SEVERE, "Property " + key + " from the library "
          + getLocation().getDescription() + " does not exist");
      throw new LibraryFormatException(getLocation());
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

  protected void addFileType(@Nonnull FileType ft) {
    fileTypes.add(ft);
  }

  protected void fillFileTypes() {
    for (FileType ft : FileType.values()) {
      try {
        String propertyName = ft.getPropertyPrefix();
        if (containsProperty(propertyName) && Boolean.parseBoolean(getProperty(propertyName))) {
          fileTypes.add(ft);
        }
      } catch (LibraryFormatException e) {
        throw new AssertionError();
      }
    }
  }

  protected void fillFiles(@Nonnull InputVDir vDir, @Nonnull FileType fileType,
      @Nonnull List<InputVFile> files) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillFiles((InputVDir) subFile, fileType, files);
      } else {
        InputVFile vFile = (InputVFile) subFile;
        if (fileType.isOfType(vFile)) {
          files.add(vFile);
        }
      }
    }
  }
}
