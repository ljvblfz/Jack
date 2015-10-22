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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * This marker indicates that a field has an associated getter.
 */
@ValidOn({JDefinedClassOrInterface.class})
@Description("This marker indicates that a field or method inside the class"
    + "is accessed from an inner class.")
public class ReferencedFromInnerClassMarker implements Marker {

  @Nonnull
  private final HashSet<JField> fields = new HashSet<JField>();

  @Nonnull
  private final HashSet<JMethod> methods = new HashSet<JMethod>();

  @Nonnull
  Set<JMethod> getMethods() {
    return methods;
  }

  @Nonnull
  Set<JField> getFields() {
    return fields;
  }

  synchronized void addField(@Nonnull JField field) {
    fields.add(field);
  }

  synchronized void addMethod(@Nonnull JMethod method) {
    methods.add(method);
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError("Not yet supported");
  }

}
