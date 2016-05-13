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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.ir.ast.JMethod;
import com.android.jack.optimizations.common.LiteralValueListTracker;
import com.android.jack.optimizations.common.SimpleLiteralValueListTracker;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * For each method's formal parameter stores a literal value if the same
 * value is passed to it in all calls of this method. If such a value
 * does not exist, (i.e. different value or non-literal expressions are
 * passed), stores this fact as well.
 *
 * Does NOT allow concurrent mutations.
 */
@Description("Represents a set of call arguments this method can be called with")
@ValidOn(JMethod.class)
public class MethodCallArgumentsMarker implements Marker {
  @Nonnull
  private final SimpleLiteralValueListTracker args;

  private MethodCallArgumentsMarker(@Nonnegative int size) {
    this.args = new SimpleLiteralValueListTracker(size);
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError();
  }

  /** Marks argument values passed to a particular method */
  public static void markArgValues(
      @Nonnull JMethod method, @Nonnull LiteralValueListTracker tracker) {
    MethodCallArgumentsMarker marker = method.getMarker(MethodCallArgumentsMarker.class);
    if (marker == null) {
      marker = new MethodCallArgumentsMarker(tracker.size());
      method.addMarker(marker);
    }
    marker.args.updateWith(tracker);
  }

  /** Returns argument tracker or null if marker is missing */
  @CheckForNull
  public static LiteralValueListTracker getTrackerAndRemoveMarker(@Nonnull JMethod method) {
    MethodCallArgumentsMarker marker = method.removeMarker(MethodCallArgumentsMarker.class);
    return marker == null ? null : marker.args;
  }
}
