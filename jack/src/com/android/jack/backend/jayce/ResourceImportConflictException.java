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

import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.Resource;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Thrown when a conflict prevents an import of a resource from a jack container.
 */
public class ResourceImportConflictException extends ImportConflictException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Resource resource;
  @Nonnull
  private final Location existingResourceLocation;
  @Nonnull
  private final JPackage pack;

  public ResourceImportConflictException(@Nonnull Resource resource,
      @Nonnull Location existingResourceLocation, @Nonnull JPackage pack) {
    this.resource = resource;
    this.existingResourceLocation = existingResourceLocation;
    this.pack = pack;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Failed to perform import: Resource '"
        + BinaryQualifiedNameFormatter.getFormatter().getName(pack, resource.getName()) + "' from "
        + resource.getLocation().getDescription() + " has already been imported from "
        + existingResourceLocation.getDescription()
        + " (see property '" + JayceFileImporter.RESOURCE_COLLISION_POLICY.getName()
        + "' for resource collision policy)";
   }
}
