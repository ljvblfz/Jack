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

package com.android.jack.tracer;

import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.BitSet;

import javax.annotation.Nonnull;

@Description("Marker for tests about Multiple tracing")
@ValidOn(JNode.class)
public class MultiTracerMarker implements Marker {

  @Nonnull
  private final BitSet set = new BitSet();

  @Nonnull
  private final BitSet mustTraceOverridingMethods = new BitSet();

  public void setMustTraceOverridingMethods(int id, boolean mustTraceOverridingMethods) {
    this.mustTraceOverridingMethods.set(id, mustTraceOverridingMethods);
  }

  public boolean mustTraceOverridingMethods(int id) {
    return mustTraceOverridingMethods.get(id);
  }

  public boolean isSet(int id) {
    return set.get(id);
  }

  public void set(int id) {
    set.set(id);
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
