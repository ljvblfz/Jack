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
import com.android.sched.vfs.InputVFile;

import javax.annotation.Nonnull;


/**
 * File types supported by jack library.
 */
public enum FileType {
  DEX("dex", ".dex") {
    @Override
    public String toString() {
      return "dex";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
    @Override
    public String getPropertyName() {
      return DexProperties.KEY_DEX;
    }
  },
  JAYCE("jayce", ".jayce") {
    @Override
    public String toString() {
      return "jayce";
    }
    @Override
    public void check() throws LibraryFormatException {
    }
    @Override
    public String getPropertyName() {
      return JayceProperties.KEY_JAYCE;
    }
  };

  @Nonnull
  private final String extension;

  @Nonnull
  private final String prefix;

  private FileType(@Nonnull String prefix, @Nonnull String extension) {
    this.prefix = prefix;
    this.extension = extension;
  }

  public abstract void check() throws LibraryFormatException;

  /**
   * Get the name of a boolean property that specify if a library contains this file type.
   * @return The property name.
   */
  public abstract String getPropertyName();

  public boolean isOfType(@Nonnull InputVFile v){
    return (v.getName().endsWith(getFileExtension()));
  }

  @Nonnull
  public String getFileExtension() {
    return extension;
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
