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

package com.android.jack.transformations.enums.opt;

import com.android.jack.ir.ast.JDefinedEnum;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

/**
 * A marker which records the total number of enum literals in the attached enum. Since this
 * information doesn't exist for library enum, we have to compute on our own.
 */
@Description("A marker which records the total number of enum literals in the attached enum.")
@ValidOn({JDefinedEnum.class})
public final class EnumFieldMarker implements Marker {

  private final int enumLiterals;

  public EnumFieldMarker(int literals) {
    enumLiterals = literals;
  }

  public int getEnumLiterals() {
    return enumLiterals;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
