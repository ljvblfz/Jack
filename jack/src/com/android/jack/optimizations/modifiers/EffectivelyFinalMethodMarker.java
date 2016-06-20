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

package com.android.jack.optimizations.modifiers;

import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/** A marker which records 'effective' final modifier on methods. */
@Description("Marker records the 'effectively final' flag on methods.")
@ValidOn({ JMethod.class })
public enum EffectivelyFinalMethodMarker implements Marker {
  /** This method is considered to be effectively final. */
  Final;

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  /** Tests if the method is effectively final */
  public static boolean isEffectivelyFinal(@Nonnull JMethod method) {
    return method.getMarker(EffectivelyFinalMethodMarker.class) != null;
  }

  /** Marks the method as effectively final */
  public static void markAsEffectivelyFinal(@Nonnull JMethod method) {
    if (!isEffectivelyFinal(method)) {
      method.addMarker(EffectivelyFinalMethodMarker.Final);
    }
  }
}
