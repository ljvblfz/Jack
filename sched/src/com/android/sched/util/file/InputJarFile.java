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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.location.FileLocation;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An class that represents a Jar.
 */
public class InputJarFile extends InputZipFile {

  public InputJarFile(@Nonnull String path)
      throws WrongPermissionException, NotFileException, NoSuchFileException, NotJarFileException {
    this(new File(path), new FileLocation(path));
  }

  public InputJarFile(@CheckForNull Directory workingDirectory, @Nonnull String path)
      throws NotFileException, WrongPermissionException, NoSuchFileException, NotJarFileException {
    this(getFileFromWorkingDirectory(workingDirectory, path),
        new FileLocation(path));
  }

  protected InputJarFile(@Nonnull File file, @Nonnull FileLocation location)
      throws WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      NotJarFileException {
    super(file, location, processJar(file, location));
  }

  @Nonnull
  private static JarFile processJar(@Nonnull File file, @Nonnull FileLocation location)
      throws NotJarFileException {
    try {
      return new JarFile(file);
    } catch (java.util.zip.ZipException e) {
      throw new NotJarFileException(location, e);
    } catch (IOException e) {
      // should not happen, because checks should already have been performed in processExisting
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  public JarFile getJarFile() {
    return (JarFile) getZipFile();
  }

  @Nonnull
  public File getFile() {
    assert file != null;
    return file;
  }
}
