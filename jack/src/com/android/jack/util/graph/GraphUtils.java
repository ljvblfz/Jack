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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Utilities for the {@link Graph} class.
 */
public class GraphUtils {

  @Nonnull
  public static <T extends GraphNode<T>> List<T> getNodesInPostOrder(@Nonnull Graph<T> graph) {
    int nodesCount = graph.getNodes().size();
    List<T> resultList = new ArrayList<T>(nodesCount);

    Deque<T> stack = new ArrayDeque<>();
    stack.addFirst(graph.getEntryNode());
    while (!stack.isEmpty()) {
      final T node = stack.peekFirst();
      assert node != null;
      T nextSuccessor = null;
      for (T succ : node.getSuccessors()) {
        if (succ != graph.getExitNode() && !stack.contains(succ) && !resultList.contains(succ)) {
          nextSuccessor = succ;
          break;
        }
      }
      if (nextSuccessor != null) {
        // We need to process this successor first.
        stack.addFirst(nextSuccessor);
      } else {
        // We processed all successors so we can add the node to the list (except special
        // entry/exit nodes).
        if (node != graph.getEntryNode() && node != graph.getExitNode()) {
          resultList.add(node);
        }
        stack.removeFirst();
      }
    }

    assert resultList.size() == nodesCount; // we must have all the nodes in the list.
    return resultList;
  }
}
