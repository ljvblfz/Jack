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

import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Imports resources.
 */
public class ResourceImporter extends ResourceOrMetaImporter {

  public ResourceImporter(@Nonnull List<InputVFS> resourceDirs) {
    super(resourceDirs);
  }

  @Override
  protected void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) {
    VPath path = new VPath(currentPath, ResourceOrMetaImporter.VPATH_SEPARATOR);
    Resource newResource = new Resource(path, file);
    session.addResource(newResource);
  }
}
