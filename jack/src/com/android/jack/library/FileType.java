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

import javax.annotation.Nonnull;


/**
 * File types supported by jack library.
 */
public enum FileType {
  PREBUILT("dex") {
    @Override
    public void check() {
    }
  },
  JAYCE("jayce") {
    @Override
    public void check() {
    }
  },
  META("meta") {
    @Override
    public void check() {
    }
  },
  RSC("resource") {
    @Override
    public void check() {
    }
  },
  DEPENDENCIES("dependencies") {
    @Override
    public void check() {
    }
  },
  LOG("logs") {
    @Override
    public void check() {
    }
  };

  @Nonnull
  private final String description;

  private FileType(@Nonnull String description) {
    this.description = description;
  }

  public abstract void check() throws LibraryFormatException;

  @Override
  public String toString() {
    return description;
  }
}
