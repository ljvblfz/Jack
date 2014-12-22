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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.stream.UncloseableInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * Class representing a input stream from a file path or a standard input.
 */
public class InputStreamFile extends AbstractStreamFile {
  public InputStreamFile(@Nonnull String name)
      throws WrongPermissionException, NotFileOrDirectoryException, NoSuchFileException {
    super(name, null /* hooks */);

    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotSetPermissionException e) {
      throw new AssertionError(e);
    }
  }

  public InputStreamFile() {
    super(Permission.READ);
  }

  @Nonnull
  public InputStream getInputStream() {
    if (file == null) {
      return new UncloseableInputStream(System.in);
    } else {
      clearRemover();
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }
  }
}