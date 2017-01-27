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

package com.android.sched.vfs;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A root of an output VFS.
 */
public abstract class AbstractOutputVFS extends AbstractVFS implements OutputVFS {
  @CheckForNull
  private OutputVDir root;

  protected void setRootDir(@Nonnull OutputVDir root) {
    this.root = root;
  }

  @Override
  @Nonnull
  public OutputVDir getRootDir() {
    assert root != null;

    return root;
  }
}

