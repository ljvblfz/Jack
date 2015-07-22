/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.ir.ast.marker;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.SerializableMarker;
import com.android.sched.marker.ValidOn;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link Marker} holds the thrown exception types.
 */
@Description("Holds the thrown exception types")
@ValidOn(JMethod.class)
public class ThrownExceptionMarker implements SerializableMarker {

  @Nonnull
  private final List<JClass> thrownExceptions;

  public ThrownExceptionMarker(@Nonnull List<JClass> thrownException) {
    assert !thrownException.isEmpty();
    this.thrownExceptions = thrownException;
  }

  @Nonnull
  public List<JClass> getThrownExceptions() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(thrownExceptions);
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

}
