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

package com.android.jack.scheduling.marker;

import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A marker which contains prebuilt dex {@link Method} of a {@link JMethod}.
 */
@Description("A marker which contains prebuilt dex Method.")
@ValidOn(JMethod.class)
public final class ImportedDexMethodMarker implements Marker {

  @Nonnull
  private final Method method;

  public ImportedDexMethodMarker(@Nonnull Method method) {
    this.method = method;
  }

  @Nonnull
  public Method getMethod() {
    return method;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
