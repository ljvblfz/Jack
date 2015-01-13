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

package com.android.sched.util.file;

import com.android.sched.util.RunnableHooks;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * Class representing a stream from a file path or a standard input/output.
 */
public class StreamFile extends AbstractStreamFile {

  public StreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      int permissions,
      @Nonnull ChangePermission change)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    super(name, hooks);

    performChecks(existence, permissions, change);
  }

  public StreamFile(int permissions) {
    super(permissions);
  }


}