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

import com.google.common.collect.Sets;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A marker which specifies how many user classes uses an enum in switch
 * statements. This mark will be updated in {@link SwitchEnumUsageCollector},
 * and finally used in {@link SyntheticClassManager}.
 */
@Description("Marker specifies how many user classes uses an enum in switch statements.")
@ValidOn(JDefinedEnum.class)
public final class SwitchEnumUsageMarker implements Marker {

  // set of classes using enum in switch statements
  @Nonnull
  private final Set<JDefinedClass> userClasses = Sets.newHashSet();

  // related enum
  private final JDefinedEnum enumType;

  /**
   * Constructor.
   * @param enumType The enum to count the uses
   */
  public SwitchEnumUsageMarker(@Nonnull JDefinedEnum enumType) {
    this.enumType = enumType;
  }

  /**
   * Add the user class into the set to avoid counting duplicated user classes.
   * @param userClass The class in which enum is used inside of switch statement
   *
   * @return true if userClass is never counted yet, otherwise return false
   */
  public boolean incrementUses(@Nonnull JDefinedClass userClass) {
    return userClasses.add(userClass);
  }
  /**
   * Get the total number of classes associated enum is used on switch statement.
   *
   * @return The total number of user classes in which enum is used on the
   * switch statement
   */
  public int getUses() {
    return userClasses.size();
  }

  /**
   * Get the associated enum.
   *
   * @return The associated enum
   */
  @Nonnull
  public JDefinedEnum getEnum() {
    return enumType;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
