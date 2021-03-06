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
 * Exception specifying that an {@link InputVFile} is not supported by the library
 */
public class UnsupportedFileTypeException extends Exception {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final InputVFile vFile;

  public UnsupportedFileTypeException(@Nonnull InputVFile vFile) {
    this.vFile = vFile;
  }

  @Override
  public String getMessage() {
    return vFile + " is not supported by the library";
  }
}
