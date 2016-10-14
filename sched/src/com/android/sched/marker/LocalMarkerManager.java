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
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 */
public class LocalMarkerManager extends AbstractMarkerManager {
  @Nonnull
  private static final Collection<Marker> EMPTY_MARKER = Collections.emptyList();

  @CheckForNull
  private ConcurrentHashMap<Class<? extends Marker>, Marker> markers;

  @Nonnull
  private final Object lock = new Object();

  @SuppressWarnings("unchecked")
  @Override
  @CheckForNull
  public <T extends Marker> T addMarker(@Nonnull T marker) {
    assert isValidMarker(marker) : "Marker '" + Items.getName(marker.getClass())
        + "' is not supported for class '" + this.getClass().getName() + "'";

    ensureMap();
    assert markers != null;

    return (T) markers.put(marker.getClass(), marker);
  }

  @Override
  public void addAllMarkers(@Nonnull Collection<Marker> collection) {
    ensureMap();

    for (Marker marker : collection) {
      assert isValidMarker(marker) : "Marker '" + Items.getName(marker.getClass())
          + "' is not supported for class '" + this.getClass().getName() + "'";
      assert markers != null;

      markers.put(marker.getClass(), marker);
    }
  }

  @Override
  @Nonnull
  public Collection<Marker> getAllMarkers() {
    synchronized (lock) {
      if (markers == null) {
        return EMPTY_MARKER;
      }
    }

    for (Marker marker : markers.values()) {
      assert checkGetAccess(marker.getClass());
    }

    assert markers != null;
    return markers.values();
  }

  @Override
  @CheckForNull
  @SuppressWarnings(value = "unchecked")
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    synchronized (lock) {
      if (markers == null) {
        return null;
      }
    }

    return (T) markers.get(cls);
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    assert isValidMarker(cls) : "Marker '" + Items.getName(cls) + "' is not supported for class '"
        + this.getClass().getName() + "'";

    synchronized (lock) {
      if (markers == null) {
        return false;
      }
    }

    return markers.containsKey(cls);
  }

  @Override
  @Nonnull
  public <T extends Marker> T getMarkerOrDefault(@Nonnull T defaultMarker) {
    assert isValidMarker(defaultMarker) : "Marker '"
        + Items.getName(defaultMarker.getClass()) + "' is not supported for class '"
        + this.getClass().getName() + "'";

    synchronized (lock) {
      if (markers == null) {
        return defaultMarker;
      }
    }

    @SuppressWarnings("unchecked")
    T marker = (T) markers.get(defaultMarker.getClass());
    if (marker == null) {
      return defaultMarker;
    } else {
      return marker;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  @CheckForNull
  public <T extends Marker> T addMarkerIfAbsent(@Nonnull T newMarker) {
    assert isValidMarker(newMarker) : "Marker '" + Items.getName(newMarker.getClass())
        + "' is not supported for class '" + this.getClass().getName() + "'";

    ensureMap();
    assert markers != null;

    return (T) markers.putIfAbsent(newMarker.getClass(), newMarker);
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  @CheckForNull
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    synchronized (lock) {
      if (markers == null) {
        return null;
      }
    }

    return (T) markers.remove(cls);
  }

  private void ensureMap() {
    synchronized (lock) {
      if (markers == null) {
        markers = new ConcurrentHashMap<Class<? extends Marker>, Marker>();
      }
    }
  }
}
