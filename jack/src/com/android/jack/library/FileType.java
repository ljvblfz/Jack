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

import com.android.jack.backend.dex.DexProperties;
import com.android.jack.jayce.JayceProperties;
import com.android.jack.preprocessor.PreprocessorProperties;
import com.android.jack.resource.ResourceProperties;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;


/**
 * File types supported by jack library.
 */
public enum FileType {
  DEX("dex", DexProperties.KEY_DEX, ".dex") {
    @Override
    public String toString() {
      return "dex";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  JAYCE("jayce", JayceProperties.KEY_JAYCE, ".jayce") {
    @Override
    public String toString() {
      return "jayce";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  JPP("jpp", PreprocessorProperties.KEY_JPP, ".jpp") {
    @Override
    public String toString() {
      return "java pre-processor";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
  },
  RSC("rsc", ResourceProperties.KEY_RESOURCE, "") {
    @Override
    public String toString() {
      return "resource";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
  };

  @Nonnull
  private final String extension;

  @Nonnull
  private final String prefix;

  @Nonnull
  private final VPath vpathPrefix;

  @Nonnull
  private final String propertyPrefix;

  private FileType(@Nonnull String vpathPrefix, @Nonnull String propertyPrefix,
      @Nonnull String extension) {
    this.prefix = vpathPrefix;
    this.vpathPrefix = new VPath(vpathPrefix, '/');
    this.propertyPrefix = propertyPrefix;
    this.extension = extension;
  }

  public abstract void check() throws LibraryFormatException;

  public boolean isOfType(@Nonnull InputVFile v){
    return (v.getName().endsWith(getFileExtension()));
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
  public String getPropertyPrefix() {
    return propertyPrefix;
  }

  @Nonnull
  public VPath getVPathPrefix() {
    return vpathPrefix;
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
}
