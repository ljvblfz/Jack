/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce;

import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Thrown when an I/O error occurred during loading of a class or interface.
 */
public class LoadIOException extends JackIOException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final JDefinedClassOrInterface notLoaded;

  public LoadIOException(@Nonnull String message, @Nonnull JDefinedClassOrInterface notLoaded) {
    super(message);
    this.notLoaded = notLoaded;
  }

  public LoadIOException(@Nonnull String message, @Nonnull JDefinedClassOrInterface notLoaded,
      @Nonnull IOException cause) {
    super(message, cause);
    this.notLoaded = notLoaded;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Failed to load "
        + Jack.getUserFriendlyFormatter().getName(notLoaded);
  }

}
