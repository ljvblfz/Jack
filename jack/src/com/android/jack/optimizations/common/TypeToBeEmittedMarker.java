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

package com.android.jack.optimizations.common;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/** Marks all types to be emitted */
@Description("Represents a type to emit flag on types")
@ValidOn(JDefinedClassOrInterface.class)
public enum TypeToBeEmittedMarker implements Marker {
  TypeToBeEmitted;

  /** Is this type to be emitted in the current session */
  public static boolean isToBeEmitted(@Nonnull JType type) {
    return type instanceof JDefinedClassOrInterface &&
        ((JDefinedClassOrInterface) type).containsMarker(TypeToBeEmittedMarker.class);
  }

  /** Marks a defined type as being emitted in the current session */
  public static void markToBeEmitted(@Nonnull JDefinedClassOrInterface type) {
    type.addMarkerIfAbsent(TypeToBeEmitted);
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
