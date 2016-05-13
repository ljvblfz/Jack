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

import com.android.jack.ir.ast.JField;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/** A marker which records 'effective' final modifier on fields. */
@Description("Marker records the 'effectively final' flag on fields.")
@ValidOn(JField.class)
public enum EffectivelyFinalFieldMarker implements Marker {
  /** This field considered to be effectively final. */
  Final;

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  /** Tests if the field is effectively final */
  public static boolean isEffectivelyFinal(@Nonnull JField field) {
    return field.getMarker(EffectivelyFinalFieldMarker.class) != null;
  }

  /** Marks the field as effectively final */
  public static void markAsEffectivelyFinal(@Nonnull JField field) {
    if (!isEffectivelyFinal(field)) {
      field.addMarker(EffectivelyFinalFieldMarker.Final);
    }
  }
}
