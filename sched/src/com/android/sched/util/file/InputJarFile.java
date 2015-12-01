/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.android.sched.util.location.FileLocation;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An class that represents a Jar.
 */
public class InputJarFile extends AbstractStreamFile {
  @Nonnull
  private final JarFile jarFile;

  public InputJarFile(@Nonnull String name)
      throws WrongPermissionException, NotFileException, NoSuchFileException, NotJarFileException {
    this(new File(name), new FileLocation(name));
  }

  public InputJarFile(@CheckForNull Directory workingDirectory, @Nonnull String string)
      throws NotFileException, WrongPermissionException, NoSuchFileException, NotJarFileException {
    this(getFileFromWorkingDirectory(workingDirectory, string), new FileLocation(string));
  }

  private InputJarFile(@Nonnull File file, @Nonnull FileLocation location)
      throws WrongPermissionException, NotFileException, NoSuchFileException, NotJarFileException {
    super(file, location, null /* hooks */);

    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }

    try {
      jarFile = new JarFile(file);
    } catch (IOException e) {
      throw new NotJarFileException(location, e);
    }
  }


  @Nonnull
  public JarFile getJarFile() {
    return jarFile;
  }

  @Nonnull
  public File getFile() {
    assert file != null;

    return file;
  }
}
