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

import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;


/**
 * Common interface for Jack libraries used as input and as output.
 */
public interface JackLibrary extends Library {

  @Nonnull
  public static final String LIBRARY_PROPERTIES = "jack.properties";

  @Nonnull
  public static final VPath LIBRARY_PROPERTIES_VPATH = new VPath(LIBRARY_PROPERTIES, '/');

  @Nonnull
  public static final String KEY_LIB_MAJOR_VERSION = "lib.version.major";

  @Nonnull
  public static final String KEY_LIB_MINOR_VERSION = "lib.version.minor";

  @Nonnull
  public static final String KEY_LIB_EMITTER = "lib.emitter";

  @Nonnull
  public static final String KEY_LIB_EMITTER_VERSION = "lib.emitter.version";

  public static final int GROUP_SIZE_FOR_DIRS = 2;

  public static final int NUM_GROUPS_FOR_DIRS = 1;

  @Nonnull
  public String getProperty(@Nonnull String key) throws LibraryFormatException;

  @Nonnull
  public boolean containsProperty(@Nonnull String key);
}
