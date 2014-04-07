/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.vfs.direct;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.config.FileLocation;
import com.android.sched.util.config.Location;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.VElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Directory in the file system.
 */
public class InputDirectDir extends AbstractVElement implements InputVDir {

  @Nonnull
  private final File dir;
  @CheckForNull
  private ArrayList<VElement> list;

  public InputDirectDir(@Nonnull File dir) throws NotFileOrDirectoryException {
    if (!dir.isDirectory()) {
      throw new NotFileOrDirectoryException(dir.getAbsolutePath(), false);
    }
    this.dir = dir;
  }

  @Nonnull
  @Override
  public String getName() {
    return dir.getName();
  }

  @Nonnull
  @Override
  public synchronized Collection<? extends VElement> list() {
    if (list == null) {
      File[] subs = dir.listFiles();
      if (subs == null) {
        throw new ConcurrentIOException(new ListDirException(dir));
      }
      if (subs.length == 0) {
        return Collections.emptyList();
      }

      list = new ArrayList<VElement>(subs.length);
      for (File sub : subs) {
        try {
          if (sub.isFile()) {
            list.add(new InputDirectFile(sub));
          } else {
            list.add(new InputDirectDir(sub));
          }
        } catch (NotFileOrDirectoryException e) {
          throw new ConcurrentIOException(e);
        }
      }
    }

    assert list != null;
    return list;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new FileLocation(dir);
  }
}
