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

package com.android.jack.util.graph;

import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A marker that represent a dominator tree in a graph.
 */
@Description("Marks the dominator parent / child relation of a CFG.")
@ValidOn(IGraphNode.class)
public class DominatorTreeMarker<N extends IGraphNode<N>> implements Marker {
  private final List<N> domChildren = new ArrayList<>();

  public static <N extends IGraphNode<N>> void addDomChild(N node, N domChild) {
    @SuppressWarnings({"cast", "unchecked"})
    DominatorTreeMarker<N> marker =
        (DominatorTreeMarker<N>) node.getMarker(DominatorTreeMarker.class);
    if (marker == null) {
      marker = new DominatorTreeMarker<N>();
      node.addMarker(marker);
    }
    marker.domChildren.add(domChild);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <N extends IGraphNode<N>> List<N> getDomChild(IGraphNode node) {
    DominatorTreeMarker marker = node.getMarker(DominatorTreeMarker.class);
    if (marker == null) {
      return Collections.EMPTY_LIST;
    } else {
      return marker.domChildren;
    }
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }

  /**
   * Removes all dominance tree marker from a graph.
   */
  public static <N extends IGraphNode<N>> void clearMarkers(@Nonnull IGraph<N> graph) {
    for (IGraphNode<?> n : graph.getNodes()) {
      n.removeMarker(DominatorTreeMarker.class);
    }
  }
}
