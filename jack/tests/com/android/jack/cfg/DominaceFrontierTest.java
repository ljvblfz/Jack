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
import com.android.jack.util.graph.DominanceFrontierInfoMarker;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Some basic tests for {@link DominanceFrontier}
 */
public class DominaceFrontierTest extends BaseGraphTestCase {
  private TestGraph graph = null;

  @Test
  public void df1()throws Exception {
    //    0
    //   / \
    //  1   2
    //  |   |
    //  3  /
    //  | /
    //  4
    //
    // 1 is the idom of 3
    // 4 is in the dominance frontier of 4
    graph = makeGraph(5);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));
    graph.connect(0, 1);
    graph.connect(0, 2);
    graph.connect(1, 3);
    graph.connect(3, 4);
    graph.connect(2, 4);
    graph.connect(graph.getNodes().get(4), graph.getExitNode());
    new DominanceFrontier<TestNode>(graph).run();

    // The only DF set that exists. Everything else should be empty.
    Assert.assertTrue(isInDominanceFrontier(1, 4));
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (i != 1 && j != 4) {
          Assert.assertFalse(isInDominanceFrontier(i, j));
        }
      }
    }
  }

  @Test
  public void df2() throws Exception {
    //    __0__
    //   /  |   \
    //  1   2   3
    //  | \     |
    //  4   5   6
    //  |   |  /
    //  | / | /
    //  7   8
    //
    graph = makeGraph(9);
    graph.connect(graph.getEntryNode(), graph.getNodes().get(0));

    // level 1
    graph.connect(0, 1);
    graph.connect(0, 2);
    graph.connect(0, 3);

    // level 2
    graph.connect(1, 4);
    graph.connect(1, 5);
    graph.connect(3, 6);

    // level 3
    graph.connect(4, 7);
    graph.connect(5, 7);
    graph.connect(5, 8);
    graph.connect(6, 8);

    graph.connect(graph.getNodes().get(7), graph.getExitNode());
    graph.connect(graph.getNodes().get(8), graph.getExitNode());

    new DominanceFrontier<TestNode>(graph).run();

    // 8 is in the DF of 1 but not 7 because 1 is still dominating 7.
    Assert.assertTrue(isInDominanceFrontier(1, 8));
    Assert.assertFalse(isInDominanceFrontier(1, 7));
  }

  private boolean isInDominanceFrontier(int parent, int target) {
    TestNode parentNode = graph.getNodes().get(parent);
    TestNode targetNode = graph.getNodes().get(target);
    return DominanceFrontierInfoMarker.isInDominanceFrontier(parentNode, targetNode);
  }
}
