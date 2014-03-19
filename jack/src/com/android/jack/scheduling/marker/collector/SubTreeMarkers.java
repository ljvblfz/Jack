/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.scheduling.marker.collector;

import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Marker containing makers of kind <T>, that was collected into the sub tree from {@code JNode}
 * containing this marker.
 *
 * @param <T> List of markers of kind <T>.
 */
@Description("SubTreeMarkers")
@ValidOn(JNode.class)
public abstract class SubTreeMarkers<T extends Marker> implements Marker {

  private static final int SUB_TREE_NOT_COMPLETELY_ANALYZED = -1;

  private int positionOfNestedMarkersEnd = SUB_TREE_NOT_COMPLETELY_ANALYZED;

  @Nonnull
  private final List<T> markers = new ArrayList<T>();

  @Nonnegative
  private int beforeMarkerEndPosition;

  @Nonnegative
  private int afterMakerStartPosition;

  @Nonnull
  private final Class<T> classOfMarker;

  public SubTreeMarkers(@Nonnull Class<T> classOfMarker) {
    this.classOfMarker = classOfMarker;
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError(this.getClass().getName() + " must not be cloned.");
  }

  void endOfNestedMarkers() {
    positionOfNestedMarkersEnd = markers.size();
  }

  void addMarker(@Nonnull T marker) {
    markers.add(marker);
  }

  void addMarkers(@Nonnull List<T> markers) {
    this.markers.addAll(markers);
  }

  boolean isCompletelyAnalyzed() {
    return positionOfNestedMarkersEnd != SUB_TREE_NOT_COMPLETELY_ANALYZED;
  }

  @Nonnegative
  int getPositionOfNestedMarkerEnd() {
    assert positionOfNestedMarkersEnd >= 0;
    return positionOfNestedMarkersEnd;
  }

  @Nonnull
  List<T> getAllMarkers() {
    return markers;
  }

  @Nonnull
  Class<T> getClassOfCollectedMarkers() {
    return classOfMarker;
  }

  @Nonnegative
  void setBeforeMarkerEndPosition(@Nonnegative int beforeMarkerEndPosition) {
    this.beforeMarkerEndPosition = beforeMarkerEndPosition;
  }

  @Nonnegative
  void setAfterMakerStartPosition(@Nonnegative int afterMakerStartPosition) {
    this.afterMakerStartPosition = afterMakerStartPosition;
  }

  @Nonnegative
  int getBeforeMarkerEndPosition() {
    return beforeMarkerEndPosition;
  }

  @Nonnegative
  int getAfterMarkerStartPosition() {
    return afterMakerStartPosition;
  }
}
