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

package com.android.jack.transformations.lambda;

import com.android.jack.ir.ast.JParameter;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * Force a parameter to be turn into a closure even if his type is not mark with
 * {@link NeedsLambdaMarker}.
 */
@Description("Force a parameter to be turn into a closure")
@ValidOn(JParameter.class)
public final class ForceClosureMarker implements Marker {

  public static final ForceClosureMarker INSTANCE = new ForceClosureMarker();

  private ForceClosureMarker() {}

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
