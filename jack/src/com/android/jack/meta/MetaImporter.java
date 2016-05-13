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

import com.android.jack.resource.ResourceOrMeta;
import com.android.jack.resource.ResourceOrMetaImporter;
import com.android.jack.resource.ResourceReadingException;
import com.android.jack.resource.StandaloneResOrMetaLocation;
import com.android.sched.util.codec.DirectoryInputVFSCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.ListPropertyId;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Imports metas.
 */
@HasKeyId
public class MetaImporter extends ResourceOrMetaImporter {

  @Nonnull
  public static final ListPropertyId<InputVFS> IMPORTED_META = new ListPropertyId<InputVFS>(
      "jack.import.meta", "Meta to import", new DirectoryInputVFSCodec()).minElements(0)
      .addDefaultValue(Collections.<InputVFS>emptyList());

  public MetaImporter(@Nonnull List<InputVFS> metaDirs) {
    super(metaDirs);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public List<Meta> getImports() {
    try {
      return (List<Meta>) super.getImports();
    } catch (ResourceReadingException e) {
      // should not happen for meta
      throw new AssertionError(e);
    }
  }

  @Override
  protected void addImportedResource(@Nonnull InputVFile file,
      @Nonnull String currentPath, @Nonnull Location resourceDirLocation,
      @Nonnull List<ResourceOrMeta> resultList) {
    VPath path = new VPath(currentPath, ResourceOrMetaImporter.VPATH_SEPARATOR);
    Meta newMeta = new Meta(path, file, new StandaloneMetaLocation(resourceDirLocation, path));
    resultList.add(newMeta);
  }

  private static class StandaloneMetaLocation extends StandaloneResOrMetaLocation {

    public StandaloneMetaLocation(@Nonnull Location baseLocation, @Nonnull VPath path) {
      super(baseLocation, path);
    }

    @Override
    @Nonnull
    public String getDescription() {
      return baseLocation.getDescription() + ", meta '" + path.getPathAsString('/') + '\'';
    }
  }
}
