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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * Utilities for the {@link Graph} class.
 */
public class GraphUtils {

  @Nonnull
  public static <T extends GraphNode<T>> List<T> getNodesInPostOrder(@Nonnull Graph<T> graph) {
    int nodesCount = graph.getNodes().size();
    List<T> resultList = new ArrayList<T>(nodesCount);
    Stack<T> stack = new Stack<>();
    stack.addAll(graph.getEntryNode().getSuccessors());
    while (!stack.isEmpty()) {
      T node = stack.pop();

      List<T> successorsNotProcessed = new ArrayList<T>();
      for (T succ : node.getSuccessors()) {
        if (succ == graph.getExitNode() || succ == node || stack.contains(succ)) {
          // Ignore exit node and graph cycles.
          continue;
        }
        if (!resultList.contains(succ) && !successorsNotProcessed.contains(succ)) {
          // The successor has not been processed yet. So we'll have to process it before the
          // current node.
          successorsNotProcessed.add(succ);
        }
      }
      if (successorsNotProcessed.isEmpty()) {
        // We put all its successors in the list so we can append the node now.
        assert !resultList.contains(node);
        resultList.add(node);
      } else {
        // Some (or all) successors have not been processed yet. We push the current node on the
        // stack first, then all the successors, to make sure successors are put in the result list
        // before the current node.
        stack.push(node);
        for (T succ : successorsNotProcessed) {
          stack.push(succ);
        }
      }
    }

    assert resultList.size() == nodesCount; // we must have all the nodes in the list.
    return resultList;
  }
}
