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

package com.android.sched.vfs.zip;

import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

class InputZipVDir extends AbstractVElement implements InputVDir {

  @Nonnull
  protected final HashMap<String, InputVElement> subs = new HashMap<String, InputVElement>();
  @Nonnull
  private final String name;

  @Nonnull
  private final Location location;

  InputZipVDir(@Nonnull String name, @Nonnull File zip, @Nonnull ZipEntry entry) {
    this.name = name;
    this.location = new ZipLocation(new FileLocation(zip), entry);
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public Collection<? extends InputVElement> list() {
    return subs.values();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Override
  public boolean isVDir() {
    return true;
  }

}
