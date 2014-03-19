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

package com.android.jack.scheduling.marker;

import com.android.jack.dx.dex.file.Code;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A marker which contains a {@code Code} instance.
 */
@Description("A marker which contains Code.")
@ValidOn(JMethod.class)
public final class DexCodeMarker implements Marker {

  @Nonnull
  private final Code code;

  public DexCodeMarker(@Nonnull Code code) {
    this.code = code;
  }

  @Nonnull
  public Code getCode() {
    return code;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
