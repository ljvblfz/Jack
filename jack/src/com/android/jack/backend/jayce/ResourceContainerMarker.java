/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.vfs.InputVFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A marker that contains resources.
 */
@Description("A marker that contains resources.")
@ValidOn(JSession.class)
public final class ResourceContainerMarker implements Marker {

  @Nonnull
  private final List<InputVFile> resourceFiles = new ArrayList<InputVFile>();

  public ResourceContainerMarker() {
  }

  @Nonnull
  public List<InputVFile> getResources() {
    return resourceFiles;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  public void addResource(@Nonnull InputVFile file) {
    boolean res = resourceFiles.add(file);
    if (!res) {
      throw new AssertionError();
    }
  }

}
