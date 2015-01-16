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

import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.Location;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * An {@link InputVDir} implementation for a {@link GenericInputVFS}.
 */
public class GenericInputVDir implements InputVDir {
  @Nonnull
  private final VDir dir;

  GenericInputVDir(@Nonnull VDir dir) {
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
        inputVElements.add(new GenericInputVDir((VDir) vElement));
      } else {
        inputVElements.add(new GenericInputVFile((VFile) vElement));
      }
    }
    return inputVElements;
  }

  @Override
  @Nonnull
  public InputVDir getInputVDir(@Nonnull VPath path) throws NotDirectoryException,
      NoSuchFileException {
    return new GenericInputVDir(dir.getVDir(path));
  }

  @Override
  @Nonnull
  public InputVFile getInputVFile(@Nonnull VPath path) throws NotFileException,
      NoSuchFileException, NotDirectoryException {
    return new GenericInputVFile(dir.getVFile(path));
  }

  @Override
  @Nonnull
  public void delete(@Nonnull VPath path) throws CannotDeleteFileException,
      NotFileException, NoSuchFileException, NotDirectoryException {
    dir.delete(dir.getVFile(path));
  }
}