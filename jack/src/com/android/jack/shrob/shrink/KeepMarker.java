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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.DynamicValidOn;
import com.android.sched.marker.Marker;

import javax.annotation.Nonnull;

/**
 * Indicates that this class or member should not be removed when shrinking.
 */
@Description("Indicates that this class or member should not be removed when shrinking.")
public class KeepMarker implements Marker {

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @DynamicValidOn
  public boolean isValidOn(@Nonnull JDefinedClassOrInterface type) {
    return !type.isExternal();
  }

  @DynamicValidOn
  public boolean isValidOn(@Nonnull JMethod method) {
    return !method.getEnclosingType().isExternal();
  }

  @DynamicValidOn
  public boolean isValidOn(@Nonnull JField field) {
    return !field.getEnclosingType().isExternal();
  }
}
