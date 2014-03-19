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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * High level node representation that can be extends towards specific kind of node.
 *
 * @param <N> The type of graph node.
 */
public class GraphNode<N extends GraphNode<N>> extends LocalMarkerManager implements Serializable {

  private static final long serialVersionUID = 1L;

  // TODO(mikaelpeltier) Think about new implementation of sparse list due to index usage and
  // append usage to fill successors.
  @Nonnull
  protected final ArrayList<N> successors = new ArrayList<N>();

  @Nonnull
  protected final ArrayList<N> predecessors = new ArrayList<N>();

  public GraphNode(@Nonnegative int successorsCount) {
    for (int i = 0; i < successorsCount; i++) {
      successors.add(null);
    }
  }

  @SuppressWarnings("unchecked")
  protected void setSuccessor(@Nonnegative int index, @Nonnull N successor) {
    if (index < successors.size() && successors.get(index) != null) {
      throw new GraphException("Successor already set.");
    }
    successors.set(index, successor);
    successor.predecessors.add((N) this);
  }

  @SuppressWarnings("unchecked")
  protected void addSuccessor(@Nonnegative int index, @Nonnull N successor) {
    successors.add(index, successor);
    successor.predecessors.add((N) this);
  }

  public void replaceBy(@Nonnull N newNode) {
    for (N pred : predecessors) {
      int index = pred.successors.indexOf(this);
      pred.successors.set(index, newNode);
    }

    newNode.predecessors.remove(this);
    newNode.predecessors.addAll(predecessors);
    predecessors.clear();
  }

  @Nonnull
  public List<N> getSuccessors() {
    List<N> filteredSuccessors = new ArrayList<N>();
    for (N succ : successors) {
      if (succ != null) {
        filteredSuccessors.add(succ);
      }
    }
    return Jack.getUnmodifiableCollections().getUnmodifiableList(filteredSuccessors);
  }

  @Nonnull
  protected List<N> getInternalSuccessors() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(successors);
  }

  @Nonnull
  public List<N> getPredecessors() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(predecessors);
  }

  public boolean removePredecessor(@Nonnull N predecessor) {
    return (predecessors.remove(predecessor));
  }
}
