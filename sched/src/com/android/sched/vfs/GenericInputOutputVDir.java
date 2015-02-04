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
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.Location;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * An {@link InputOutputVDir} implementation for a {@link GenericInputOutputVFS}.
 */
public class GenericInputOutputVDir implements InputOutputVDir {
  @Nonnull
  private final VDir dir;

  GenericInputOutputVDir(@Nonnull VDir dir) {
    this.dir = dir;
  }

  @Override
  public boolean isVDir() {
    return true;
  }

  @Override
  @Nonnull
  public String getName() {
    return dir.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return dir.getLocation();
  }

  @Override
  @Nonnull
  public Collection<? extends InputVElement> list() {
    Collection<? extends VElement> vElements = dir.list();
    Collection<InputVElement> inputVElements = new ArrayList<InputVElement>(vElements.size());
    for (VElement vElement : vElements) {
      if (vElement.isVDir()) {
        inputVElements.add(new GenericInputOutputVDir((VDir) vElement));
      } else {
        inputVElements.add(new GenericInputOutputVFile((VFile) vElement));
      }
    }
    return inputVElements;
  }

  @Override
  @Nonnull
  public InputOutputVDir getInputVDir(@Nonnull VPath path) throws NotDirectoryException,
      NoSuchFileException {
    return new GenericInputOutputVDir(dir.getVDir(path));
  }

  @Override
  @Nonnull
  public InputOutputVFile getInputVFile(@Nonnull VPath path) throws NotFileException,
      NoSuchFileException, NotDirectoryException {
    return new GenericInputOutputVFile(dir.getVFile(path));
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    return new GenericOutputVFile(dir.createVFile(path));
  }

  @Override
  @Nonnull
  public OutputVDir createOutputVDir(@Nonnull VPath path) throws CannotCreateFileException {
    // XXX Why not GenericOutputVDir
    return new GenericInputOutputVDir(dir.createVDir(path));
  }
}