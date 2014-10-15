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
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * Exception specifying that the binary of type {@link BinaryKind} for a
 * {@link VPath} representing a type does not exist at {@link Location}.
 */
public class BinaryDoesNotExistException extends Exception {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Location location;

  @Nonnull
  private final VPath typePath;

  @Nonnull
  private final BinaryKind binaryKind;

  public BinaryDoesNotExistException(@Nonnull Location location,
      @Nonnull VPath typePath, @Nonnull BinaryKind binaryKind) {
    this.location = location;
    this.typePath = typePath;
    this.binaryKind = binaryKind;
  }

  @Override
  public String getMessage() {
    return binaryKind + " binary does not exist for "
        + typePath.getPathAsString('.') + " in "
        + location.getDescription();
  }
}
