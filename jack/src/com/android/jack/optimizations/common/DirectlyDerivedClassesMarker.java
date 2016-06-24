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

package com.android.jack.optimizations.common;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;

/**
 * Marker is used to represent a list of directly derived defined
 * subclasses of a defined class.
 *
 * IMPORTANT: only classes included in types-to-emit of the current
 * session are getting marked with this marker.
 */
@Description("Marker is used to represent a list of directly derived subclasses.")
@ValidOn(JDefinedClass.class)
public class DirectlyDerivedClassesMarker implements Marker {
  @Nonnull
  private final ConcurrentLinkedQueue<JDefinedClass> directlyDerivedClasses =
      new ConcurrentLinkedQueue<>();

  private DirectlyDerivedClassesMarker() {
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  @Nonnull
  /** Returns a list of directly derived subclasses of a given defined class */
  public static Collection<JDefinedClass> getDirectlyDerivedClasses(@Nonnull JDefinedClass clazz) {
    DirectlyDerivedClassesMarker marker = clazz.getMarker(DirectlyDerivedClassesMarker.class);
    return marker == null
        ? Collections.<JDefinedClass>emptyList()
        : Jack.getUnmodifiableCollections()
            .getUnmodifiableCollection(marker.directlyDerivedClasses);
  }

  /** Returns true if the class has any derived subclasses */
  public static boolean hasDirectlyDerivedClasses(@Nonnull JDefinedClass clazz) {
    DirectlyDerivedClassesMarker marker = clazz.getMarker(DirectlyDerivedClassesMarker.class);
    return marker != null && !marker.directlyDerivedClasses.isEmpty();
  }

  /** Mark directly derived class */
  public static void markDirectlyDerivedClass(
      @Nonnull JDefinedClass clazz, @Nonnull JDefinedClass derived) {
    // Note that 'clazz' is only set on types to be emitted
    assert clazz.isToEmit();

    DirectlyDerivedClassesMarker marker =
        clazz.getMarker(DirectlyDerivedClassesMarker.class);
    if (marker == null) {
      marker = new DirectlyDerivedClassesMarker();
      DirectlyDerivedClassesMarker existing = clazz.addMarkerIfAbsent(marker);
      if (existing != null) {
        marker = existing;
      }
    }
    marker.directlyDerivedClasses.add(derived);
  }
}
