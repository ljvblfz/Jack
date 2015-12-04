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

import com.android.sched.item.Items;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 * Similar to {@link StaticMarkerManager} but its data structures are stored in each instance.
 */
public class LocalMarkerManager extends AbstractMarkerManager {
  @Nonnull
  private static final Collection<Marker> EMPTY_MARKER = new LinkedList<Marker>();

  @CheckForNull
  private Map<Class<? extends Marker>, Marker> markers;

  @Nonnull
  private final Object lock = new Object();

  @Override
  public void addMarker(@Nonnull Marker marker) {
    synchronized (lock) {
      assert isValidMarker(marker.getClass()) : "Marker '" + Items.getName(marker.getClass())
                                                + "' is not supported for class '"
                                                + this.getClass().getName() + "'";

      // assert checkAddAccess(marker.getClass());

      if (markers == null) {
        markers = new HashMap<Class<? extends Marker>, Marker>();
      }

      assert markers.get(marker.getClass())
             == null : "Marker '" + Items.getName(marker.getClass()) + "' already exists";

      markers.put(marker.getClass(), marker);
    }
  }

  public void addAllMarker(@Nonnull Collection<Marker> collection) {
    assert collection != null;

    Iterator<Marker> iter = collection.iterator();
    while (iter.hasNext()) {
      addMarker(iter.next());
    }
  }

  @Override
  @Nonnull
  public Collection<Marker> getAllMarkers() {
    synchronized (lock) {
      if (markers == null) {
        return EMPTY_MARKER;
      }

      for (Marker marker : markers.values()) {
        assert checkGetAccess(marker.getClass());
      }

      // FINDBUGS
      assert markers != null;
      return markers.values();
    }
  }

  @Override
  @CheckForNull
  @SuppressWarnings(value = "unchecked")
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    synchronized (lock) {
      // assert checkGetAccess(cls);

      if (markers == null) {
        return null;
      }

      return (T) markers.get(cls);
    }
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    synchronized (lock) {
      assert isValidMarker(cls) : "Marker '" + Items.getName(cls) + "' is not supported for class '"
                                  + this.getClass().getName() + "'";

      if (markers == null) {
        return false;
      }

      return markers.containsKey(cls);
    }
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    synchronized (lock) {
      // assert checkRemoveAccess(cls);

      if (markers == null) {
        return null;
      }

      return (T) markers.remove(cls);
    }
  }
}
