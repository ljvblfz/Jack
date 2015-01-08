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

import com.android.sched.vfs.InputOutputVFS;

import java.util.Properties;

import javax.annotation.Nonnull;


/**
 * Abstract class representing an output jack library.
 */
public abstract class OutputJackLibrary extends CommonJackLibrary implements OutputLibrary {

  @Nonnull
  protected final InputOutputVFS baseVFS;

  public OutputJackLibrary(@Nonnull InputJackLibrary inputJackLibrary, @Nonnull String emitterId,
      @Nonnull String emitterVersion) {
    super(inputJackLibrary.libraryProperties);
    setEmitter(emitterId, emitterVersion);
    this.baseVFS = (InputOutputVFS) inputJackLibrary.baseVFS;
  }

  public OutputJackLibrary(@Nonnull InputOutputVFS baseVFS, @Nonnull String emitterId,
      @Nonnull String emitterVersion) {
    super(new Properties());
    setEmitter(emitterId, emitterVersion);
    this.baseVFS = baseVFS;
  }

  private void setEmitter(String emitterId, String emitterVersion) {
    putProperty(KEY_LIB_EMITTER, emitterId);
    putProperty(KEY_LIB_EMITTER_VERSION, emitterVersion);
    putProperty(KEY_LIB_MAJOR_VERSION, String.valueOf(getMajorVersion()));
    putProperty(KEY_LIB_MINOR_VERSION, String.valueOf(getMinorVersion()));
  }
}
