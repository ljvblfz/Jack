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
 * <p>
 * A {@link MarkerManager} can be marked by multiple {@link Marker}. However it can be
 * marked by at most one instance of a {@link Marker} class.
 */
public interface MarkerManager {
  /**
   * Indicates whether this object is marked by a marker of the given class {@code T}.
   *
   * @param <T> the marker type
   * @param cls the class of the marker
   * @return {@code true} if the object is marked by a marker of this class;
   *         {@code false} otherwise
   */
  <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls);

  /**
   * Returns the marker of the given class {@code T} or {@code null} if there is none.
   *
   * @param <T> the marker type
   * @param cls the class of the marker
   * @return the marker of the given class or {@code null}
   */
  @CheckForNull
  <T extends Marker> T getMarker(@Nonnull Class<T> cls);

  /**
   * Returns an existing marker of type {@code T} or the given {@code defaultMarker}
   * if there is none.
   *
   * @param <T> the marker type
   * @param defaultMarker the marker being returned if there is no marker of type {@code T}
   * @return the existing marker of type {@code T} or the given {@code defaultMarker}
   */
  @Nonnull
  <T extends Marker> T getMarkerOrDefault(@Nonnull T defaultMarker);

  /**
   * Returns all markers on this object.
   *
   * @return a collection of markers
   */
  @Nonnull
  Collection<Marker> getAllMarkers();

  /**
   * Adds the given {@code marker} on this object and returns the previous marker of this class
   * or {@code null} if there is none.
   *
   * @param <T> the marker type
   * @param marker the marker to add on this object
   * @return the previous marker or {@code null} if there was none.
   */
  @CheckForNull
  <T extends Marker> T addMarker(@Nonnull T marker);

  /**
   * Adds the given {@code marker} on this object except if there is already a marker of the
   * same class. This method returns the existing marker of the same class or {@code null}
   * if there is none.
   *
   * @param <T> the marker type
   * @param marker the marker to add on this object
   * @return the existing marker or {@code null} if there was none.
   */
  @CheckForNull
  <T extends Marker> T addMarkerIfAbsent(@Nonnull T marker);

  /**
   * Adds multiple markers on this object. If there is more than one marker of the same type in the
   * collection, the result is unspecified. It is the responsibility of the caller to ensure
   * that the collection does not contain multiple instances of the same marker type.
   *
   * @param collection a collection of marker to add
   */
  void addAllMarkers(@Nonnull Collection<Marker> collection);

  /**
   * Removes the marker of the given class and returns that marker or {@code null} if
   * there was none.
   *
   * @param <T> the marker type
   * @param cls the class of the marker to remove
   * @return the marker that is removed or {@code null} if there was no marker of the given
   * class.
   */
  @CheckForNull
  <T extends Marker> T removeMarker(@Nonnull Class<T> cls);
}
