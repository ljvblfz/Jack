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

package com.android.jack.ir.impl;

import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * Keep the method found by the frontend during the resolution.
 */
@Description("Keep the method found by the frontend during the resolution.")
@ValidOn(JMethodCall.class)
public class ResolutionTargetMarker implements Marker {

  @Nonnull
  private JMethod target;

  public ResolutionTargetMarker(@Nonnull JMethod target) {
    this.target = target;
  }

  @Nonnull
  public JMethod getTarget() {
    return target;
  }

  public void resolve(@Nonnull JMethod target) {
    this.target = target;
  }

  @Nonnull
  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

}
