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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 * Similar to {@link LocalMarkerManager} but its data structures are stored statically in a
 * {@code HashMap}.
 */
public class MarkerManager extends AbstractMarkerManager {
  @Nonnull
  private static final Map<Class<? extends Marker>, Map<MarkerManager, Marker>> mapByMarker =
      new HashMap<Class<? extends Marker>, Map<MarkerManager, Marker>>();

  @Override
  public void addMarker(@Nonnull Marker marker) {
    assert isValidMarker(marker.getClass()) : "Marker '" + Items.getName(marker.getClass())
        + "' is not supported for class '" + this.getClass().getName() + "'";

    assert checkAddAccess(marker.getClass());

    Map<MarkerManager, Marker> mapByMarked = mapByMarker.get(marker.getClass());
    if (mapByMarked == null) {
      mapByMarked = new HashMap<MarkerManager, Marker>();
      mapByMarker.put(marker.getClass(), mapByMarked);
    }

    assert mapByMarked.get(this) == null : "Marker '" + Items.getName(marker.getClass())
        + "' already exists";

    mapByMarked.put(this, marker);
  }

  public void addAllMarker(@Nonnull Collection<Marker> collection) {
    assert collection != null;

    Iterator<Marker> iter = collection.iterator();
    while (iter.hasNext()) {
      addMarker(iter.next());
    }
  }

  @Nonnull
  @Override
  public Collection<Marker> getAllMarkers() {
    Collection<Marker> c = new ArrayList<Marker>();

    for (Map<MarkerManager, Marker> mapByMarked : mapByMarker.values()) {
      if (mapByMarked.get(this) != null) {
        Marker marker = mapByMarked.get(this);

        assert checkGetAccess(marker.getClass());
        c.add(marker);
      }
    }

    return c;
  }

  @Override
  @CheckForNull
  @SuppressWarnings(value = "unchecked")
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    assert isValidMarker(cls) : "Marker '" + Items.getName(cls) + "' is not supported for class '"
        + this.getClass().getName() + "'";

    Map<MarkerManager, Marker> mapByMarked = mapByMarker.get(cls);
    if (mapByMarked == null) {
      return null;
    }

    // TODO(mikaelpeltier) Re-enable sanity check support when getMarker will be supported even if
    // there is no need on the marker.
    // assert checkGetAccess(cls);

    return (T) mapByMarked.get(this);
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    assert isValidMarker(cls) : "Marker '" + Items.getName(cls) + "' is not supported for class '"
        + this.getClass().getName() + "'";

    Map<MarkerManager, Marker> mapByMarked = mapByMarker.get(cls);
    if (mapByMarked == null) {
      return false;
    }

    return mapByMarked.containsKey(this);
  }

  @Override
  @CheckForNull
  @SuppressWarnings(value = "unchecked")
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    assert isValidMarker(cls) : "Marker '" + Items.getName(cls) + "' is not supported for class '"
        + this.getClass().getName() + "'";

    Map<MarkerManager, Marker> mapByMarked = mapByMarker.get(cls);
    if (mapByMarked == null) {
      return null;
    }

    assert checkRemoveAccess(cls);

    return (T) mapByMarked.remove(this);
  }
}
