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

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;

import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Virtual file of a {@link VFS}.
 */
public interface VFile extends VElement, InputStreamProvider, OutputStreamProvider {
  @Nonnull
  VPath getPath();

  @Nonnull
  OutputStream getOutputStream(boolean append) throws WrongPermissionException;

  @CheckForNull
  String getDigest();

  long getLastModified();

  void delete() throws CannotDeleteFileException;
  @Nonnull
  VPath getPathFromRoot();

  void copy(@Nonnull VFile vFile) throws WrongPermissionException,
      CannotCloseException, CannotReadException, CannotWriteException;
}
