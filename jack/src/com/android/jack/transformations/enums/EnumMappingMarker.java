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

package com.android.jack.transformations.enums;

import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A marker which contains mapping between JEnumField and constant use into case statement.
 */
@Description("A marker which contains mapping between JEnumField and constant use " +
    "into case statement.")
@ValidOn(JMethod.class)
public final class EnumMappingMarker implements Marker {

  @Nonnull
  private final Map<JFieldId, Integer> enumFieldToSwitchValue =
      new HashMap<JFieldId, Integer>();

  public EnumMappingMarker() {
  }

  public void addMapping(@Nonnull JFieldId enumField, int value) {
    enumFieldToSwitchValue.put(enumField, Integer.valueOf(value));
  }

  @Nonnull
  public Map<JFieldId, Integer> getMapping() {
    return enumFieldToSwitchValue;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
