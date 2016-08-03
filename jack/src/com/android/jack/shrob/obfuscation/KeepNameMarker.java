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

package com.android.jack.shrob.obfuscation;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPackage;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * Indicates that this class or member should not be renamed when obfuscating.
 */
@Description("Indicates that this class or member should not be renamed.")
@ValidOn(value = {JPackage.class, JDefinedClassOrInterface.class, JFieldId.class,
    JMethodIdWide.class})
public class KeepNameMarker implements Marker {

  @Nonnull
  public static final KeepNameMarker INSTANCE = new KeepNameMarker();

  private KeepNameMarker() {
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
