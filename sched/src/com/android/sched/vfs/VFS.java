/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.vfs;

import com.android.sched.util.HasDescription;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.location.HasLocation;

import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A VFS.
 */
public interface VFS extends HasLocation, AutoCloseable, HasDescription {
  @Nonnull
  String getPath();

  @Nonnull
  VDir getRootDir();

  boolean needsSequentialWriting();

  @Nonnull
  Set<Capabilities> getCapabilities();

  @CheckForNull
  String getDigest();

  boolean isClosed();

  @Override
  void close() throws CannotCloseException;

  @CheckForNull
  String getInfoString();
}
