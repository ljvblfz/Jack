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

import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * An {@link InputVFS} that is a part of another {@link InputVFS}, prefixed by a path. The parent
 * {@link InputVFS} is still the one that needs to be closed.
 */
public class PrefixedInputVFS extends AbstractInputVFS {

  public PrefixedInputVFS(@Nonnull InputVFS inputVFS, @Nonnull VPath path)
      throws NotDirectoryException, NoSuchFileException {
    InputVDir previousRootDir = inputVFS.getRootInputVDir();
    setRootDir(previousRootDir.getInputVDir(path));
  }

  @Override
  @Nonnull
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return getRootInputVDir().getLocation();
  }

  @Override
  public void close() {
    // do not actually close
  }
}
