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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JMethodIdWide;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * This marker indicates that all calls to method with this id need adjustment because
 * of OptimizedInnerAccessorGenerator work.
 */
@ValidOn(JMethodIdWide.class)
@Description("This marker indicates that all calls to method with this id need adjustment because"
    + "of OptimizedInnerAccessorGenerator work.")
public class NeedsDispatchAdjustment implements Marker {

  @Nonnull
  public static final NeedsDispatchAdjustment INSTANCE = new NeedsDispatchAdjustment();

  private NeedsDispatchAdjustment() {

  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

}
