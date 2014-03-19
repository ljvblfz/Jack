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

package com.android.sched.marker;

import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;

@Name(Marker6.NAME)
@Description(Marker6.DESCRIPTION)
@ValidOn(MarkedA.class)
@OnlyFor(SchedTest.class)
public class Marker6 implements Marker {
  public static final String NAME = "Marker 6";
  public static final String DESCRIPTION = "Marker 6 description";

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
