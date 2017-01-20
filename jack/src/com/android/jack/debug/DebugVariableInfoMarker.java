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

package com.android.jack.debug;

import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JSsaVariableDefRef;
import com.android.jack.ir.ast.JSsaVariableUseRef;
import com.android.jack.ir.ast.JType;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.SerializableMarker;
import com.android.sched.marker.ValidOn;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link Marker} contains debug information related to variable.
 */
@ValidOn({JLocalRef.class, JSsaVariableDefRef.class, JSsaVariableUseRef.class})
@Description("This marker contains debug information related to variable.")
public class DebugVariableInfoMarker implements SerializableMarker {

  @Nonnull
  private final String name;

  @Nonnull
  private final JType type;

  @CheckForNull
  private String genericSignature;

  public DebugVariableInfoMarker(@Nonnull String name, @Nonnull JType type,
      @CheckForNull String genericSignature) {
    this.name = name;
    this.type = type;
    this.genericSignature = genericSignature;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public JType getType() {
    return type;
  }

  @CheckForNull
  public String getGenericSignature() {
    return genericSignature;
  }

  public void setGenericSignature(@CheckForNull String genericSignature) {
    this.genericSignature = genericSignature;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
