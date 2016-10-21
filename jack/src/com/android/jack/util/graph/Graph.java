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

package com.android.jack.util.graph;

import com.android.jack.Jack;
import com.android.sched.marker.LocalMarkerManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * High level graph representation that must be extends toward a specific graph.
 *
 * @param <N> The type of node contained by the graph.
 */
public abstract class Graph<N extends GraphNode<N>> extends LocalMarkerManager implements IGraph<N>{

  @Nonnull
  private final List<N> nodes;

  @Nonnull
  private final N entry;

  @Nonnull
  private final N exit;

  public Graph(@Nonnull N entry, @Nonnull N exit, @Nonnull ArrayList<N> nodes) {
    assert entry != null;
    assert exit != null;
    assert nodes != null;
    this.entry = entry;
    this.exit = exit;
    this.nodes = Jack.getUnmodifiableCollections().getUnmodifiableList(nodes);
  }

  @Nonnull
  @Override
  public List<N> getNodes() {
    return nodes;
  }

  /**
   * @return the entry
   */
  @Nonnull
  @Override
  public N getEntryNode() {
    return entry;
  }

  /**
   * @return the exit
   */
  @Nonnull
  @Override
  public N getExitNode() {
    return exit;
  }
}
