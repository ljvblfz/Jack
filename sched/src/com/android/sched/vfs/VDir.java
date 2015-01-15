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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.HasLocation;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Virtual directory of a {@link VFS}.
 */
public interface VDir extends VElement, HasLocation {
  @Nonnull
  VPath getPath();

  @Nonnull
  Collection<? extends VElement> list();

  @Nonnull
  VDir getVDir(@Nonnull VPath path) throws NotDirectoryException,
      NoSuchFileException;

  @Nonnull
  VFile getVFile(@Nonnull VPath path) throws NotFileException,
      NoSuchFileException, NotDirectoryException;

  @Nonnull
  void delete(@Nonnull VFile file) throws CannotDeleteFileException;

  @Nonnull
  VDir createVDir(@Nonnull VPath path) throws CannotCreateFileException;

  @Nonnull
  VFile createVFile(@Nonnull VPath path) throws CannotCreateFileException;

  @Nonnull
  VDir getVDir(@Nonnull String name) throws NotDirectoryException, NoSuchFileException;

  @Nonnull
  VFile getVFile(@Nonnull String name) throws NotFileException, NoSuchFileException;
}
