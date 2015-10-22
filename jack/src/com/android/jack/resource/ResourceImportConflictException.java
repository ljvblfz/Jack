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

package com.android.jack.resource;

import com.android.jack.backend.jayce.ImportConflictException;
import com.android.jack.ir.ast.Resource;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Thrown when a conflict prevents an import of a resource.
 */
public class ResourceImportConflictException extends ImportConflictException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Location newResourceLocation;
  @Nonnull
  private final Resource existingResource;

  public ResourceImportConflictException(@Nonnull Resource existingResource,
      @Nonnull Location newResourceLocation) {
    this.newResourceLocation = newResourceLocation;
    this.existingResource = existingResource;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Resource \'" + existingResource.getPath().getPathAsString('/') + "\' from "
        + newResourceLocation.getDescription() + " has already been imported from "
        + existingResource.getLocation().getDescription()
        + " (see property '" + ResourceImporter.RESOURCE_COLLISION_POLICY.getName()
        + "' for resource collision policy)";
   }
}
