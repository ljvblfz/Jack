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

package com.android.jack.backend.dex;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A {@link Marker} containing a number that defines merging order for types.
 */
@Description(
    "Contains a number that defines merging order for types.")
@ValidOn(value = {JDefinedClassOrInterface.class})
public class NumberMarker implements Marker {

  private final int number;

  public NumberMarker(int number) {
    this.number = number;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  public int getNumber() {
    return number;
  }

}
