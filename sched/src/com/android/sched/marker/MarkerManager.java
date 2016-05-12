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

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 */
public interface MarkerManager {
  <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls);

  @CheckForNull
  <T extends Marker> T getMarker(@Nonnull Class<T> cls);
  @Nonnull
  <T extends Marker> T getMarkerOrDefault(@Nonnull T defaultMarker);
  @Nonnull
  Collection<Marker> getAllMarkers();

  @CheckForNull
  <T extends Marker> T addMarker(@Nonnull T marker);
  @CheckForNull
  <T extends Marker> T addMarkerIfAbsent(@Nonnull T marker);
  void addAllMarker(@Nonnull Collection<Marker> collection);

  @CheckForNull
  <T extends Marker> T removeMarker(@Nonnull Class<T> cls);
}
