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

import com.google.common.collect.Lists;

import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

/**
 * A marker that represent a dominator tree in a graph.
 */
@ValidOn(IGraphNode.class)
public class DominatorTreeMarker implements Marker {
  @SuppressWarnings("rawtypes")
  private final List<IGraphNode> domChildren = new ArrayList<>();

  @SuppressWarnings("rawtypes")
  public static void addDomChild(IGraphNode node, IGraphNode domChild) {
    DominatorTreeMarker marker = node.getMarker(DominatorTreeMarker.class);
    if (marker == null) {
      marker = new DominatorTreeMarker();
      node.addMarker(marker);
    }
    marker.domChildren.add(domChild);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <N extends IGraphNode<N>> List<N> getDomChild(IGraphNode node) {
    DominatorTreeMarker marker = node.getMarker(DominatorTreeMarker.class);
    if (marker == null) {
      return Lists.newArrayListWithCapacity(0);
    } else {
      List<N> result = Lists.newArrayListWithCapacity(marker.domChildren.size());
      for (IGraphNode n : marker.domChildren) {
        result.add((N) n);
      }
      return result;
    }
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }
}
