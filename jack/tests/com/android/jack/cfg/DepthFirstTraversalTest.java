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

import com.android.jack.util.graph.DepthFirstTraversal;
import com.android.jack.util.graph.GraphNodeVisitor;

import junit.framework.Assert;

import org.junit.Test;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tests for basic depth first traversal.
 */
public class DepthFirstTraversalTest extends BaseGraphTestCase {

  @Test
  public void dft1() throws Exception {
    //   0
    // /   \
    // 1    2
    // |    |
    // 3   /
    // | /
    // 4
    //
    // 1 is the idom of 3
    // 4 is in the dominance frontier of 4
    TestGraph graph = makeGraph(6);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(0, 2);
    graph.connect(1, 3);
    graph.connect(3, 4);
    graph.connect(2, 4);
    graph.connect(graph.getNodes().get(4), graph.getExitNode());
    Assert.assertEquals("02413", doDepthFirstTraversal(graph, false));
  }

  @Test
  public void dftReverse1() throws Exception {
    //   0
    // /   \
    // 1    2
    // |    |
    // 3   /
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

    Assert.assertEquals("42031", doDepthFirstTraversal(graph, true));
  }

  @Test
  public void dftReverse2() throws Exception {
    //   0
    // /   \
    // 1    2
    // |
    // 3
    // |
    // 4
    TestGraph graph = makeGraph(5);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(0, 2);
    graph.connect(1, 3);
    graph.connect(3, 4);
    graph.connect(graph.getNodes().get(4), graph.getExitNode());
    graph.connect(graph.getNodes().get(2), graph.getExitNode());
    Assert.assertEquals("20431", doDepthFirstTraversal(graph, true));
  }

  @Nonnull
  private static String doDepthFirstTraversal(final TestGraph graph, boolean reverse) {
    final StringBuilder sb = new StringBuilder();
    DepthFirstTraversal.run(graph, reverse, new GraphNodeVisitor<TestNode>() {
      @Override
      public void visitNode(@Nonnull TestNode v, @CheckForNull TestNode parent) {
        if (v != graph.getEntryNode() && v != graph.getExitNode()) {
          sb.append(v);
        }
      }
    });
    return sb.toString();
  }
}