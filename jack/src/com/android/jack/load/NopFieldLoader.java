/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.load;

import com.android.jack.ir.ast.JField;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import javax.annotation.Nonnull;

/**
 * A {@link FieldLoader} doing nothing.
 */
public class NopFieldLoader extends AbstractFieldLoader {

  @Nonnull
  public static final FieldLoader INSTANCE = new NopFieldLoader();
  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  private NopFieldLoader() {
    // Nothing to do
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JField loaded) {
    return NO_LOCATION;
  }

  @Override
  protected void ensureAll(@Nonnull JField loaded) {
    // Nothing to do
  }
}