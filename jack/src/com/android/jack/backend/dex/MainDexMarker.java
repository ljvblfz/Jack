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

package com.android.jack.backend.dex;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

/**
 * A {@link Marker} that indicates that a class or interface must be included in the main dex in
 * multidex mode.
 */
@Description(
    "Indicates that a class or interface must be included in the main dex in multidex mode")
@ValidOn(value = {JDefinedClassOrInterface.class})
public class MainDexMarker implements Marker {

  public static final MainDexMarker INSTANCE = new MainDexMarker();

  private MainDexMarker() {
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

}
