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

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A marker which records the enum fields related to an enum.
 */
@Description("A marker which records the enum fields related to an enum.")
@ValidOn({JDefinedEnum.class})
public final class EnumFieldMarker implements Marker {

  @Nonnull
  private final List<JEnumField> enumFields = Lists.newArrayList();

  // are the fields already sorted by alphabetical order
  private boolean areFieldsSorted = false;

  public EnumFieldMarker() {}

  /**
   * Attach an enum field to the related enum.
   * @param enumField the enum field to be added
   */
  public void addEnumField(@Nonnull JEnumField enumField) {
    assert !enumFields.contains(enumField);
    enumFields.add(enumField);
  }

  /**
   * Sort enum fields based on their name alphabetically.
   */
  public void sortEnumFields() {
    if (!areFieldsSorted) {
      Collections.sort(enumFields, new Comparator<JEnumField>() {
        @Override
        public int compare(JEnumField field1, JEnumField field2) {
          return field1.getName().compareTo(field2.getName());
        }
      });
      areFieldsSorted = true;
    }
  }

  /**
   * Get the enum fields no matter they are sorted or not.
   * @return the enum fields
   */
  @Nonnull
  public List<JEnumField> getEnumFields() {
    return enumFields;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
