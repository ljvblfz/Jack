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

package com.android.jack.library;

import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Location describing a type into an input library.
 */
public class TypeInInputLibraryLocation extends Location {

  @Nonnull
  private final InputLibrary inputLib;

  @Nonnull
  private final String typeName;

  public TypeInInputLibraryLocation(@Nonnull InputLibrary inputLib, @Nonnull String typeName) {
    this.inputLib = inputLib;
    this.typeName = typeName;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return inputLib.getLocation().getDescription() + ", type '" + typeName + '\'';
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof TypeInInputLibraryLocation
        && ((TypeInInputLibraryLocation) obj).inputLib.equals(inputLib)
        && ((TypeInInputLibraryLocation) obj).typeName.equals(typeName);
  }

  @Override
  public final int hashCode() {
    return inputLib.hashCode() ^ typeName.hashCode();
  }

  @Nonnull
  public InputLibrary getInputLibrary() {
    return inputLib;
  }
}
