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
 * Class describing an input library location.
 */
public class InputLibraryLocation extends Location {

  @Nonnull
  private final InputLibrary inputLibrary;

  public InputLibraryLocation(@Nonnull InputLibrary inputLibrary) {
    this.inputLibrary = inputLibrary;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "library " + inputLibrary.getInputVDir().getLocation().getDescription();
  }

  @Nonnull
  public InputLibrary getInputLibrary() {
    return inputLibrary;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof InputLibraryLocation
        && ((InputLibraryLocation) obj).inputLibrary.equals(inputLibrary);
  }

  @Override
  public final int hashCode() {
    return inputLibrary.hashCode();
  }
}
