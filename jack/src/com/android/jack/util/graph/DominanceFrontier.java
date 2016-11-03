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

import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * Calculates the dominance-frontiers of a method's basic blocks. Algorithm from "A Simple, Fast
 * Dominance Algorithm" by Cooper, Harvey, and Kennedy; transliterated to Java.
 */
@Transform(add = {DominanceFrontierInfoMarker.class, DominatorTreeMarker.class})
@Constraint(need = {NodeListMarker.class})
@Use(Dominators.class)
public class DominanceFrontier<N extends IGraphNode<N>> {

  @Nonnull
  private final IGraph<N> graph;

  @Nonnull
  private final ImmutableList<N> nodes;

  /**
   * Constructs instance. Call {@link DominanceFrontier#run} to process.
   *
   * @param graph {@code non-null;} Control flow graph to process
   */
  public DominanceFrontier(@Nonnull IGraph<N> graph) {
    this.graph = graph;
    this.nodes = NodeListMarker.getNodeList(graph);
    int szNodes = nodes.size();
    for (N bb : nodes) {
      DominanceFrontierInfoMarker.setDomInfo(bb, szNodes);
    }
  }

  /**
   * Calculates the dominance frontier information for the method.
   */
  @Nonnull
  public void run() {
    Dominators.make(graph, false);
    buildDomTree();
    calcDomFronts();
  }

  /**
   * The dominators algorithm leaves us knowing who the immediate dominator is for each node. This
   * sweeps the node list and builds the proper dominance tree.
   */
  private void buildDomTree() {
    for (N node : nodes) {
      N idom = DominanceFrontierInfoMarker.getIDom(node);
      if (idom == null) {
        continue;
      }
      DominatorTreeMarker.addDomChild(idom, node);
    }
  }

  /**
   * Calculates the dominance-frontier set. from "A Simple, Fast Dominance Algorithm" by Cooper,
   * Harvey, and Kennedy; transliterated to Java.
   */
  private void calcDomFronts() {
    for (N nb : nodes) {
      //final DomInfo nbInfo = domInfos[getIdByNode(nb)];
      final N nbIdom = DominanceFrontierInfoMarker.getIDom(nb);
      int numPreds = 0;
      for (@SuppressWarnings("unused") N pred : nb.getPredecessorsIterable()) {
        numPreds++;
      }

      if (numPreds > 1) {
        for (N pred : nb.getPredecessorsIterable()) {
          for (N runner = pred; runner != nbIdom; /* empty */) {
            /*
             * We can stop if we hit a block we already added label to, since we must be at a part
             * of the dom tree we have seen before.
             */
            if (runner == null) {
              break;
            }

            if (DominanceFrontierInfoMarker.isInDominanceFrontier(runner, nb)) {
              break;
            }

            // Add b to runner's dominance frontier set.
            DominanceFrontierInfoMarker.addDominanceFrontier(runner, nb);
            runner = DominanceFrontierInfoMarker.getIDom(runner);
          }
        }
      }
    }
  }
}
