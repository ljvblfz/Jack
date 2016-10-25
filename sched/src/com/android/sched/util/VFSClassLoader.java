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

package com.android.sched.util;

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.LocationByteStreamSucker;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * ClassLoader loading from a {@link InputVFS}
 */
public class VFSClassLoader extends ClassLoader {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  protected final InputVFS vfs;

  public VFSClassLoader(@Nonnull InputVFS vfs, @CheckForNull ClassLoader parentClassLoader) {
    super(parentClassLoader);
    this.vfs = vfs;
  }

  @CheckForNull
  @Override
  public InputStream getResourceAsStream(@Nonnull String name) {
    VPath path = new VPath(name, '/');
    InputVFile vFile;
    try {
      vFile = vfs.getRootInputVDir().getInputVFile(path);
      return vFile.getInputStream();
    } catch (WrongPermissionException e) {
      logger.log(Level.INFO, "Failed to open resource '" + name + "' from "
          + vfs.getLocation().getDescription(), e);
      return null;
    } catch (NotFileOrDirectoryException | NoSuchFileException e) {
      return null;
    }
  }

  @Nonnull
  @Override
  protected Class<?> findClass(@Nonnull String name) throws ClassNotFoundException {
    VPath path = new VPath(name, '.');
    path.addSuffix(".class");
    InputVFile vFile;
    try {
      vFile = vfs.getRootInputVDir().getInputVFile(path);
    } catch (NotFileOrDirectoryException | NoSuchFileException e) {
      throw new ClassNotFoundException(name, e);
    }

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      try (InputStream is = vFile.getInputStream()) {
        new LocationByteStreamSucker(is, byteStream, vFile.getLocation(), NoLocation.getInstance())
        .suck();

      } catch (IOException e) {
        throw new CannotCloseException(vFile, e);
      }
    } catch (CannotWriteException e) {
      // Cannot happen with a ByteArrayOutputStream
      throw new AssertionError(e);
    } catch (CannotCloseException | CannotReadException e) {
      logger.log(Level.SEVERE, "Failed to load class '" + name + "' from "
          + vfs.getLocation().getDescription(), e);
      throw new ClassNotFoundException(name, e);
    } catch (WrongPermissionException e) {
      logger.log(Level.INFO, "Failed to load class '" + name + "' from "
          + vfs.getLocation().getDescription(), e);
      throw new ClassNotFoundException(name, e);
    }
    byte[] byteArray = byteStream.toByteArray();
    return defineClass(name, byteArray, 0, byteArray.length);
  }

  @CheckForNull
  @Override
  protected URL findResource(@Nonnull String name) {
    throw new UnsupportedOperationException();
  }

}
