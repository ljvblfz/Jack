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

import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * This marker indicates that a field has an associated getter.
 */
@ValidOn(JMethod.class)
@Description("This marker indicates that this method became static in inner class"
    + "accessors generation, and it has to be rewritten to use its last argument instead"
    + "of 'this'.")
public class NeedsRethising implements Marker {

  @Nonnull
  public static final NeedsRethising INSTANCE = new NeedsRethising();

  private NeedsRethising() {

  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

}
