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

import com.google.common.collect.Lists;

import com.android.jack.Options;
import com.android.jack.util.graph.AssignGraphNodeIds;
import com.android.jack.util.graph.AssigntGraphNodeList;
import com.android.jack.util.graph.IGraph;
import com.android.jack.util.graph.IGraphNode;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.marker.Marker;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Utilities to conduct some unit tests on a simple graph.
 *
 */
public abstract class BaseGraphTestCase {

  /**
   * Creates a graph for testing purposes.
   */
  protected static TestGraph makeGraph(@Nonnegative int nNodes) {
    List<TestNode> nodes = Lists.newArrayList();
    for (int i = 0; i < nNodes; i++) {
      nodes.add(new TestNode(i));
    }
    TestNode entry = new TestNode(nNodes);
    TestNode exit = new TestNode(nNodes + 1);
    nodes.add(entry);
    nodes.add(exit);
    TestGraph graph = new TestGraph(entry, exit, nodes);
    AssignGraphNodeIds.assignIds(graph);
    AssigntGraphNodeList.assignNodeList(graph);
    return graph;
  }

  /**
   * Simple node type that represents a number which is same as the node's label.
   *
   * Note that this label is purely for testing purpose and it is totally independent on any sort
   * of "ID" a graph or graph algorithm use for traversal and other computations.
   */
  protected static class TestNode extends LocalMarkerManager implements IGraphNode<TestNode> {
    private final int label;
    private final List<TestNode> predecessors = Lists.newArrayList();
    private final List<TestNode> successors = Lists.newArrayList();

    public TestNode(@Nonnegative int label) {
      this.label = label;
    }

    /**
     * For testing purposing, everything is a valid marker.
     */
    @Override
    protected boolean isValidMarker(@Nonnull Marker marker) {
      return true;
    }

    @Override
    public String toString() {
      return "" + label;
    }

    @Override
    public Iterable<TestNode> getSuccessorsIterable() {
      return successors;
    }

    @Override
    @Nonnull
    public Iterable<TestNode> getPredecessorsIterable() {
      return predecessors;
    }
  }

  /**
   * Simple graph data structure for testing.
   */
  protected static class TestGraph extends LocalMarkerManager implements IGraph<TestNode> {
    private final List<TestNode> nodes;
    private final @Nonnull TestNode entry;
    private final @Nonnull TestNode exit;
    protected TestGraph(@Nonnull TestNode entry, @Nonnull TestNode exit,
        @Nonnull List<TestNode> nodes) {
      this.nodes = nodes;
      this.entry = entry;
      this.exit = exit;
    }

    /**
     * For testing purposing, everything is a valid marker.
     */
    @Override
    protected boolean isValidMarker(@Nonnull Marker marker) {
      return true;
    }

    @Nonnull
    protected TestNode getNode(@Nonnegative int index) {
      return nodes.get(index);
    }

    protected void connect(@Nonnegative int from, @Nonnegative int to) {
      TestNode fromNode = getNode(from);
      TestNode toNode = getNode(to);
      connect(fromNode, toNode);
    }

    protected void connect(@Nonnegative TestNode fromNode, @Nonnegative TestNode toNode) {
      fromNode.successors.add(toNode);
      toNode.predecessors.add(fromNode);
    }

    protected void assertConnected(@Nonnegative int from, @Nonnegative int to) {
      TestNode fromNode = getNode(from);
      TestNode toNode = getNode(to);
      Assert.assertTrue(fromNode.successors.contains(toNode));
      Assert.assertTrue(toNode.predecessors.contains(fromNode));
    }

    protected void assertConnected(@Nonnull TestNode fromNode, @Nonnull TestNode toNode) {
      Assert.assertTrue(fromNode.successors.contains(toNode));
      Assert.assertTrue(toNode.predecessors.contains(fromNode));
    }

    protected void assertNotConnected(@Nonnegative int from, @Nonnegative int to) {
      TestNode fromNode = getNode(from);
      TestNode toNode = getNode(to);
      Assert.assertFalse(fromNode.successors.contains(toNode));
      Assert.assertFalse(toNode.predecessors.contains(fromNode));
    }

    @Override
    @Nonnull
    public List<TestNode> getNodes() {
      return nodes;
    }

    @Override
    @Nonnull
    public TestNode getEntryNode() {
      return entry;
    }

    @Override
    @Nonnull
    public TestNode getExitNode() {
      return exit;
    }
  }

  @Before
  public void setUp() throws Exception {
    Options options = new Options();
    RunnableHooks hooks = new RunnableHooks();
    options.checkValidity(hooks);
    Config config = options.getConfig();
    ThreadConfig.setConfig(config);
  }

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }
}