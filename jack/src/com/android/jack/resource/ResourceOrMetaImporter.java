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

import com.android.jack.ir.ast.JSession;
import com.android.jack.lookup.JLookup;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Imports resources or metas.
 */
public abstract class ResourceOrMetaImporter {

  protected static final char VPATH_SEPARATOR = JLookup.PACKAGE_SEPARATOR;

  @Nonnull
  private final List<InputVFS> resourceDirs;

  public ResourceOrMetaImporter(@Nonnull List<InputVFS> resourceDirs) {
    this.resourceDirs = resourceDirs;
  }

  public void doImport(@Nonnull JSession session) throws ResourceReadingException {
    try {
      for (InputVFS resourceDir : resourceDirs) {
        importResourceDirElement(resourceDir.getRootInputVDir().list(), session, "");
      }
    } catch (ResourceImportConflictException e) {
      throw new ResourceReadingException(e);
    }
  }

  private void importResourceDirElement(@Nonnull Collection<? extends InputVElement> elements,
      @Nonnull JSession session, @Nonnull String currentPath)
      throws ResourceImportConflictException {
    for (InputVElement element : elements) {
      String path = currentPath + element.getName();
      if (element.isVDir()) {
        importResourceDirElement(((InputVDir) element).list(), session, path + VPATH_SEPARATOR);
      } else {
        InputVFile file = (InputVFile) element;
        addImportedResource(file, session, path);
      }
    }
  }

  protected abstract void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) throws ResourceImportConflictException;
}
