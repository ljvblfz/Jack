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
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.StandardInputLocation;
import com.android.sched.util.stream.UncloseableInputStream;
import com.android.sched.vfs.InputStreamProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a input stream from a file path or a standard input.
 */
public class InputStreamFile extends AbstractStreamFile implements InputStreamProvider {
  @CheckForNull
  private InputStream stream;

  public InputStreamFile(@Nonnull String name)
      throws WrongPermissionException, NotFileException, NoSuchFileException {
    this(new File(name), new FileLocation(name));
  }

  public InputStreamFile() {
    super(new StandardInputLocation());
    stream = new UncloseableInputStream(System.in);
  }

  public InputStreamFile(@Nonnull InputStream in, @Nonnull Location location) {
    super(location);
    this.stream = new UncloseableInputStream(in);
  }

  public InputStreamFile(@CheckForNull Directory workingDirectory, @Nonnull String string)
      throws NotFileException, WrongPermissionException, NoSuchFileException {
    this(getFileFromWorkingDirectory(workingDirectory, string), new FileLocation(string));
  }

  private InputStreamFile(@Nonnull File file, @Nonnull FileLocation location)
      throws WrongPermissionException, NotFileException, NoSuchFileException {
    super(file, location, null /* hooks */);

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

  @Override
  @Nonnull
  public InputStream getInputStream() {
    if (stream == null) {
      clearRemover();

      try {
        stream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return stream;
  }
}