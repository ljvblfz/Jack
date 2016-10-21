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

package com.android.jack.cfg;

import com.android.jack.util.graph.DominanceFrontier;
import com.android.jack.util.graph.DominatorDepthFirstTraversal;
import com.android.jack.util.graph.GraphNodeVisitor;

import junit.framework.Assert;

import org.junit.Test;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tests for {@link DominatorDepthFirstTraversal}.
 */
public class DominatorDepthFirstTraversalTest extends BaseGraphTestCase {
  @Test
  public void ddft1() throws Exception {
    //  0
    // / \
    // 1  2
    // |  |
    // 3  /
    // | /
    // 4
    //
    TestGraph graph = makeGraph(5);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(0, 2);
    graph.connect(1, 3);
    graph.connect(3, 4);
    graph.connect(2, 4);
    graph.connect(graph.getNodes().get(4), graph.getExitNode());
    Assert.assertEquals("01324", doDominatorDepthFirstTraversal(graph));
  }

  @Test
  public void ddft2() throws Exception {
    //  0
    // /
    // 1
    // | \
    // 2  3
    // |   \
    // 4   5
    // |  /
    // | /
    // 6
    TestGraph graph = makeGraph(7);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(1, 2);
    graph.connect(1, 3);
    graph.connect(2, 4);
    graph.connect(3, 5);
    graph.connect(4, 6);
    graph.connect(5, 6);
    graph.connect(graph.getNodes().get(6), graph.getExitNode());
    Assert.assertEquals("0124356", doDominatorDepthFirstTraversal(graph));
  }

  @Test
  public void ddft3() throws Exception {
    // This creates a loop.
    // 0 -> 1 -> 2 -> 1.
    TestGraph graph = makeGraph(7);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(1, 2);
    graph.connect(2, 1);
    Assert.assertEquals("012", doDominatorDepthFirstTraversal(graph));
  }

  @Nonnull
  private static String doDominatorDepthFirstTraversal(final TestGraph graph) {
    new DominanceFrontier<>(graph).run();
    final StringBuilder sb = new StringBuilder();
    DominatorDepthFirstTraversal.run(graph, new GraphNodeVisitor<TestNode>() {
      @Override
      public void visitNode(@Nonnull TestNode v, @CheckForNull TestNode parent) {
        // Parent is always null since it is meaning less in this context.
        Assert.assertNull(parent);
        if (v != graph.getEntryNode() && v != graph.getExitNode()) {
          sb.append(v);
        }
      }
    });
    return sb.toString();
  }
}