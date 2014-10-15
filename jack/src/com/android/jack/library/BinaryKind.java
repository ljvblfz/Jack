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

import javax.annotation.Nonnull;


/**
 * Binary kind supported by jack library.
 */
public enum BinaryKind {
  DEX(".dex") {
    @Override
    public String toString() {
      return "dex";
    }
  };

  @Nonnull
  private final String extension;

  private BinaryKind(@Nonnull String extension) {
    this.extension = extension;
  }

  public boolean isBinaryFile(@Nonnull InputVFile v){
    return (v.getName().endsWith(getFileExtension()));
  }

  @Nonnull
  public String getFileExtension() {
    return extension;
  }

  @Nonnull
  public static BinaryKind getBinaryKind(@Nonnull InputVFile v) throws NotBinaryException {
    for (BinaryKind kind : BinaryKind.values()) {
      if (kind.isBinaryFile(v)) {
        return kind;
      }
    }

    throw new NotBinaryException(v);
  }
}
