/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.jack.backend.jayce.JayceFileImporter.CollisionPolicy;
import com.android.jack.config.id.Arzon;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.sched.util.codec.DirectoryInputVFSCodec;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ListPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Imports resources.
 */
@HasKeyId
public class ResourceImporter extends ResourceOrMetaImporter {

  @Nonnull
  public static final PropertyId<CollisionPolicy> RESOURCE_COLLISION_POLICY = PropertyId
      .create(
          "jack.import.resource.policy",
          "Defines the policy to follow concerning resource collision",
          new EnumCodec<CollisionPolicy>(CollisionPolicy.class, CollisionPolicy.values())
              .ignoreCase()).addDefaultValue(CollisionPolicy.FAIL).addCategory(Arzon.class);

  @Nonnull
  public static final ListPropertyId<InputVFS> IMPORTED_RESOURCES = new ListPropertyId<InputVFS>(
      "jack.import.resource", "Resources to import", new DirectoryInputVFSCodec())
      .on(File.pathSeparator).minElements(0).addDefaultValue(Collections.<InputVFS>emptyList());

  @Nonnull
  private final CollisionPolicy resourceCollisionPolicy =
      ThreadConfig.get(RESOURCE_COLLISION_POLICY);

  public ResourceImporter(@Nonnull List<InputVFS> resourceDirs) {
    super(resourceDirs);
  }

  @Override
  protected void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) throws ResourceImportConflictException {
    VPath path = new VPath(currentPath, ResourceOrMetaImporter.VPATH_SEPARATOR);
    Resource newResource = new Resource(path, file, new StandaloneResourceLocation(file));
    for (Resource existingResource : session.getResources()) {
      if (existingResource.getPath().equals(path)) {
        if (resourceCollisionPolicy == CollisionPolicy.FAIL) {
          throw new ResourceImportConflictException(existingResource, newResource.getLocation());
        } else {
          session.getUserLogger().log(Level.INFO,
              "Resource in {0} has already been imported from {1}: ignoring import", new Object[] {
                  newResource.getLocation().getDescription(),
                  existingResource.getLocation().getDescription()});
        }
        return;
      }
    }
    session.addResource(newResource);
  }

  private static class StandaloneResourceLocation implements Location {
    @Nonnull
    private final InputVFile file;

    public StandaloneResourceLocation(@Nonnull InputVFile file) {
      this.file = file;
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "resource " + file.getLocation().getDescription();
    }
  }
}
