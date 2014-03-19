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

import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * High level graph representation that must be extends toward a specific graph.
 *
 * @param <N> The type of node contained by the graph.
 */
public class Graph<N extends GraphNode<N>> {

  @Nonnull
  private final LinkedList<N> nodes = new LinkedList<N>();

  @CheckForNull
  protected N entry = null;

  @CheckForNull
  protected N exit = null;

  /**
   * @return the nodes
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  public List<N> getNodes() {
    return (List<N>) nodes.clone();
  }

  /**
   * @return the entry
   */
  @Nonnull
  public N getEntryNode() {
    assert entry != null;
    return entry;
  }

  /**
   * @return the exit
   */
  @Nonnull
  public N getExitNode() {
    assert exit != null;
    return exit;
  }

  public void addNode(@Nonnull N node) {
    nodes.add(node);
  }

  public boolean removeNode(@Nonnull N node) {
    boolean remove = nodes.remove(node);
    if (remove) {
      for (N succ : node.getSuccessors()) {
        succ.removePredecessor(node);
      }
    }
    return remove;
  }

}
