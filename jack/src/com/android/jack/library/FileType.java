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

import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * File types supported by jack library.
 */
public enum FileType {
  DEX("dex", "dex", ".dex", "dex") {
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  JAYCE("jayce", "jayce", ".jayce", "jayce") {
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  JPP("jpp", "jpp", ".jpp", "java pre-processor") {
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  RSC("rsc", "rsc", "", "resource") {
    @Override
    public void check() throws LibraryFormatException {
    }
  };

  @Nonnull
  private final String description;

  @Nonnull
  private final String extension;

  @Nonnull
  private final String prefix;

  @Nonnull
  private final VPath vpathPrefix;

  @Nonnull
  private final String propertyPrefix;

  private FileType(@Nonnull String vpathPrefix, @Nonnull String propertyPrefix,
      @Nonnull String extension, @Nonnull String description) {
    this.prefix = vpathPrefix;
    this.vpathPrefix = new VPath(vpathPrefix, '/');
    this.propertyPrefix = propertyPrefix;
    this.extension = extension;
    this.description = description;
  }

  public abstract void check() throws LibraryFormatException;

  public boolean isOfType(@Nonnull InputVFile v){
    return (v.getName().endsWith(getFileExtension()));
  }

  @Override
  public String toString() {
    return description;
  }

  @Nonnull
  public String getFileExtension() {
    return extension;
  }

  @Nonnull
  public String getPrefix() {
    return prefix;
  }

  @Nonnull
  public String buildPropertyName(@CheckForNull String suffix) {
    return (propertyPrefix + (suffix == null ? "" : suffix));
  }

  @Nonnull
  public VPath buildDirVPath(@Nonnull VPath vpath) {
    return getPathWithPrefix(vpath);
  }

  @Nonnull
  public VPath buildFileVPath(@Nonnull VPath vpath) {
    VPath clonedPath = getPathWithPrefix(vpath);
    clonedPath.addSuffix(getFileExtension());
    return clonedPath;
  }

  @Nonnull
  public static FileType getFileType(@Nonnull InputVFile v) throws UnsupportedFileTypeException {
    for (FileType fileType : FileType.values()) {
      if (fileType.isOfType(v)) {
        return fileType;
      }
    }

    throw new UnsupportedFileTypeException(v);
  }

  @Nonnull
  private VPath getPathWithPrefix(@Nonnull VPath vpath) {
    VPath clonedPath = vpath.clone();
    clonedPath.prependPath(vpathPrefix);
    return clonedPath;
  }
}
