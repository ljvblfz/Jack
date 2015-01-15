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
import com.android.sched.util.file.WrongPermissionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A base implementation of a {@link VFS}.
 */
abstract class BaseVFS<DIR extends BaseVDir, FILE extends BaseVFile> implements VFS {
  @Override
  @Nonnull
  public abstract DIR getRootDir();

  @Nonnull
  abstract InputStream openRead(@Nonnull FILE file) throws WrongPermissionException;

  @Nonnull
  abstract OutputStream openWrite(@Nonnull FILE file) throws WrongPermissionException;

  @Nonnull
  abstract void delete(@Nonnull FILE file) throws CannotDeleteFileException;

  @Nonnull
  abstract Collection<? extends BaseVElement> list(DIR dir);

  @Nonnull
  abstract FILE createVFile(@Nonnull DIR parent, @Nonnull String name)
      throws CannotCreateFileException;

  @Nonnull
  abstract DIR createVDir(@Nonnull DIR parent, @Nonnull String name)
      throws CannotCreateFileException;

  @Nonnull
  abstract DIR getVDir(@Nonnull DIR parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException;

  @Nonnull
  abstract FILE getVFile(@Nonnull DIR parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException;
}
