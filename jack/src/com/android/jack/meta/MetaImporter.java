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

package com.android.jack.meta;

import com.android.jack.ir.ast.JSession;
import com.android.jack.resource.ResourceOrMetaImporter;
import com.android.jack.resource.ResourceReadingException;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Imports metas.
 */
public class MetaImporter extends ResourceOrMetaImporter {

  public MetaImporter(@Nonnull List<InputVFS> metaDirs) {
    super(metaDirs);
  }

  @Override
  public void doImport(@Nonnull JSession session) {
    try {
      super.doImport(session);
    } catch (ResourceReadingException e) {
      // should not happen for meta
      throw new AssertionError(e);
    }
  }

  @Override
  protected void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) {
    VPath path = new VPath(currentPath, ResourceOrMetaImporter.VPATH_SEPARATOR);
    Meta newMeta = new Meta(path, file);
    session.addMeta(newMeta);
  }
}
