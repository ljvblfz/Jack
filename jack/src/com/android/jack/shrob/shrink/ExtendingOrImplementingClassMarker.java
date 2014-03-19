/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Marker containing all the classes extending or implementing the marked type
 */
@Description("Marker containing all the classes extending or implementing the marked type")
@ValidOn({JDefinedClassOrInterface.class, JPhantomClassOrInterface.class})
public class ExtendingOrImplementingClassMarker implements Marker {
  @Nonnull
  private final List<JDefinedClass> extendingOrImplementingClasses = new ArrayList<JDefinedClass>();

  public void addSubClass(@Nonnull JDefinedClass extendingOrImplementingClass) {
    extendingOrImplementingClasses.add(extendingOrImplementingClass);
  }

  @Nonnull
  public List<JDefinedClass> getExtendingOrImplementingClasses() {
    return extendingOrImplementingClasses;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

}
