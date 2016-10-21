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
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * A basic depth first traversal of a directed graph.
 */
public class DepthFirstTraversal<N extends IGraphNode<N>> {
  @Nonnull
  private final IGraph<N> graph;

  @Nonnull
  private final GraphNodeVisitor<N> visitor;

  private final boolean reverse;

  @Nonnull
  private final ImmutableList<N> nodes;

  /**
   * Performance a Depth First Traversal on a graph using a provided visitor starting from the root
   * node.
   *
   * @param graph Give Graph.
   * @param reverse If true, reverse the direction of all the edges and start from the end node.
   * @param visitor
   */
  public static <N extends IGraphNode<N>> void run(@Nonnull IGraph<N> graph, boolean reverse,
      @Nonnull GraphNodeVisitor<N> visitor) {
    DepthFirstTraversal<N> dft = new DepthFirstTraversal<N>(graph, reverse, visitor);
    dft.run();
  }

  private DepthFirstTraversal(@Nonnull IGraph<N> graph, boolean reverse,
      @Nonnull GraphNodeVisitor<N> visitor) {
    this.graph = graph;
    this.reverse = reverse;
    this.visitor = visitor;
    this.nodes = NodeListMarker.getNodeList(graph);
  }

  private void run() {
    BitSet visited = new BitSet(nodes.size());

    // We push the parent first, then the child on the stack.
    Stack<N> stack = new Stack<N>();

    N rootBlock = reverse ? graph.getExitNode() : graph.getEntryNode();

    stack.add(null); // Start with null parent.
    stack.add(rootBlock);

    while (stack.size() > 0) {
      N cur = stack.pop();
      N parent = stack.pop();

      if (!visited.get(NodeIdMarker.getId(cur))) {
        for (N child : reverse ? cur.getPredecessorsIterable() : cur.getSuccessorsIterable()) {
          stack.add(cur);
          stack.add(child);
        }
        visited.set(NodeIdMarker.getId(cur));
        visitor.visitNode(cur, parent);
      }
    }
  }
}
