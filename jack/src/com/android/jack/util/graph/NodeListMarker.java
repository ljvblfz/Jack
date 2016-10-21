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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.List;

import javax.annotation.Nonnull;

/**
 *
 */
@ValidOn(JControlFlowGraph.class)
public class NodeListMarker implements Marker {

  @SuppressWarnings("rawtypes")
  private final ImmutableList<IGraphNode> orderedList;

  public NodeListMarker(@SuppressWarnings("rawtypes") List<IGraphNode> orderedList) {
    this.orderedList = ImmutableList.copyOf(orderedList);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Nonnull
  public static <N extends IGraphNode<N>> ImmutableList<N> getNodeList(@Nonnull IGraph<N> graph) {
    NodeListMarker marker = graph.getMarker(NodeListMarker.class);
    assert marker != null;
    Builder<N> builder = ImmutableList.<N>builder();
    for (IGraphNode node : marker.orderedList) {
      builder.add((N) node);
    }
    return builder.build();
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }
}
