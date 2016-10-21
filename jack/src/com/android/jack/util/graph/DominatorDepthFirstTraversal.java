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

import java.util.BitSet;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * A traversal that visits a directed graph by dominator order in a depth first manner.
 */
public class DominatorDepthFirstTraversal<N extends IGraphNode<N>> {

  @Nonnull
  private final IGraph<N> graph;

  @Nonnull
  private final GraphNodeVisitor<N> visitor;

  @Nonnull
  private final ImmutableList<N> nodes;

  /**
   * Visits each reachable node of the graph in a dominator order and invoke the visitor's visit
   * method on it. Each visit call will have null as parent.
   */
  public static <N extends IGraphNode<N>> void run(@Nonnull IGraph<N> graph,
      @Nonnull GraphNodeVisitor<N> visitor) {
    DominatorDepthFirstTraversal<N> ddft = new DominatorDepthFirstTraversal<N>(graph, visitor);
    ddft.run();
  }

  private DominatorDepthFirstTraversal(@Nonnull IGraph<N> graph,
      @Nonnull GraphNodeVisitor<N> visitor) {
    this.graph = graph;
    this.visitor = visitor;
    this.nodes = NodeListMarker.getNodeList(graph);
  }

  private void run() {
    BitSet visited = new BitSet(nodes.size());
    Stack<N> stack = new Stack<N>();

    stack.add(graph.getEntryNode());

    while (stack.size() > 0) {
      N cur = stack.pop();

      List<N> curDomChildren = DominatorTreeMarker.getDomChild(cur);

      int curId = NodeIdMarker.getId(cur);
      if (!visited.get(curId)) {
        // We walk the tree this way for historical reasons...
        for (int i = curDomChildren.size() - 1; i >= 0; i--) {
          N child = curDomChildren.get(i);
          stack.add(child);
        }
        visited.set(curId);
        visitor.visitNode(cur, /* parent = */ null);
      }
    }
  }
}
