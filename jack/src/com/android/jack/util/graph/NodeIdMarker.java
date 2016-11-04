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

import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.MarkerManager;
import com.android.sched.marker.ValidOn;

import java.util.BitSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link Marker} to represent a numeric ID for a basic block.
 *
 * The goal is to allow sets of blocks to be represented by {@link BitSet} by assigning a number to
 * each basic block.
 */
@Description("A numeric ID for a node in a graph.")
@ValidOn(JBasicBlock.class)
public class NodeIdMarker implements Marker {
  @Nonnegative
  private final int id;

  public NodeIdMarker(@Nonnegative int id) {
    this.id = id;
  }

  @Nonnegative
  public static int getId(@Nonnull MarkerManager node) {
    NodeIdMarker marker = node.getMarker(NodeIdMarker.class);
    assert marker != null;
    return marker.id;
  }

  public static void setId(@Nonnull MarkerManager node, @Nonnegative int id) {
    assert node.getMarker(NodeIdMarker.class) == null;
    node.addMarker(new NodeIdMarker(id));
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  /**
   * Assign numberic IDs to a graph.
   */
  public static void assignIds(@Nonnull IGraph<?> graph) {
    int index = 0;
    for (IGraphNode<?> n : graph.getNodes()) {
      NodeIdMarker.setId(n, index++);
    }
  }

  /**
   * Removes all numberic IDs from a graph.
   */
  public static void removeIds(@Nonnull IGraph<?> graph) {
    for (IGraphNode<?> n : graph.getNodes()) {
      n.removeMarker(NodeIdMarker.class);
    }
  }
}