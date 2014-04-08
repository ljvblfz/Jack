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

package com.android.jack.ir.ast;

import com.android.jack.load.AbstractMethodLoader;
import com.android.jack.load.MethodLoader;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import javax.annotation.Nonnull;

/**
 * A {@link MethodLoader} doing nothing.
 */
public class NopMethodLoader extends AbstractMethodLoader {

  @Nonnull
  public static final MethodLoader INSTANCE = new NopMethodLoader();
  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  private NopMethodLoader() {
    // Nothing to do
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JMethod loaded) {
    return NO_LOCATION;
  }

  @Override
  protected void ensureAll(@Nonnull JMethod loaded) {
    // Nothing to do
  }
}