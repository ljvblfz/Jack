/*
 * Copyright (C) 2015 The Android Open Source Project
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

import javax.annotation.Nonnull;


/**
 * A base implementation of a {@link VElement}.
 */
public abstract class BaseVElement implements VElement {
  @Nonnull
  protected BaseVFS<BaseVDir, BaseVFile>  vfs;
  @Nonnull
  protected final String name;

  @SuppressWarnings("unchecked")
  BaseVElement(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs,
      @Nonnull String name) {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
    this.name = name;
  }

  @SuppressWarnings("unchecked")
  void changeVFS(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs) {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  BaseVFS<BaseVDir, BaseVFile> getVFS() {
    return vfs;
  }
}
