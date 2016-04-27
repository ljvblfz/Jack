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

package com.android.jack.library;

import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Class describing a library location.
 */
public class LibraryLocation extends Location {

  @Nonnull
  private final Location sourceLocation;

  public LibraryLocation(@Nonnull Location sourceLocation) {
    this.sourceLocation = sourceLocation;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return sourceLocation.getDescription();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof LibraryLocation) {
      return sourceLocation.equals(((LibraryLocation) obj).sourceLocation);
    }

    return false;
  }

  @Override
  public final int hashCode() {
    return sourceLocation.hashCode();
  }
}
