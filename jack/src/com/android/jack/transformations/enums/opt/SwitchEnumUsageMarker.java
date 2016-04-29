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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JPackage;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A marker which specifies how many user classes uses an enum in switch statements under
 * a package. This mark will be updated in {@link SwitchEnumUsageCollector}, and finally
 * used in {@link SyntheticClassManager}.
 */
@Description(
    "Marker specifies how many user classes uses an enum in switch statements under a package.")
@ValidOn(JPackage.class)
public final class SwitchEnumUsageMarker implements Marker {

  // the map from the classes using enum to the corresponding enums
  @Nonnull
  private final Multimap<JDefinedClass, JDefinedEnum> userClasses = HashMultimap.create();

  @Nonnull
  private final JPackage enclosingPackage;

  /**
   * Constructor.
   * @param enclosingPackage the package to count the uses
   */
  public SwitchEnumUsageMarker(@Nonnull JPackage enclosingPackage) {
    this.enclosingPackage = enclosingPackage;
  }

  /**
   * Add the user class into userClasses map.
   *
   * @param userClass the class in which enum is used inside of switch statement
   * @param enumType the enum which is used inside of switch statement
   *
   * @return true if userClass is never counted yet, otherwise return false
   */
  public synchronized boolean addEnumUsage(
      @Nonnull JDefinedClass userClass, @Nonnull JDefinedEnum enumType) {
    return userClasses.put(userClass, enumType);
  }
  /**
   * Get the total number of pairs (user class, enum) under the associated package in which
   * enum is used.
   *
   * @return the total number of enum uses
   */
  @Nonnegative
  public synchronized int getUses() {
    return userClasses.size();
  }

  /**
   * Get the set of used enums type.
   * @return the set of enums used in switch statement
   */
  public synchronized Set<JDefinedEnum> getUsedEnumsType() {
    Set<JDefinedEnum> usedEnumsType = Sets.newHashSet();
    for (JDefinedEnum enumType : userClasses.values()) {
      usedEnumsType.add(enumType);
    }
    return usedEnumsType;
  }

  /**
   * Get the associated package.
   *
   * @return The associated package
  */
  @Nonnull
  public JPackage getPackage() {
    return enclosingPackage;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
