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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.JackUserException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.util.config.Location;

import javax.annotation.Nonnull;

/**
 * Thrown when a conflict prevents an import of a jack file.
 */
public class ImportConflictException extends JackUserException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final JDefinedClassOrInterface existingType;
  @Nonnull
  private final Location failedToImportSource;

  public ImportConflictException(@Nonnull JDefinedClassOrInterface existingType,
      @Nonnull Location failedToImportSource) {
    this.existingType = existingType;
    this.failedToImportSource = failedToImportSource;
  }

  @Override
  @Nonnull
  public String getMessage() {
    Location existingSource = existingType.getLocation();
    return "Failed to perform import: Type "
        + Jack.getUserFriendlyFormatter().getName(existingType) + " from "
        + failedToImportSource.getDescription() + " has already been imported from "
        + existingSource.getDescription()
        + " (see property '" + JayceFileImporter.COLLISION_POLICY.getName()
        + "' for collision policy)";
   }
}
