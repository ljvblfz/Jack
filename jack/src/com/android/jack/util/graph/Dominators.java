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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class computes dominator and post-dominator information using the Lengauer-Tarjan method.
 *
 * See A Fast Algorithm for Finding Dominators in a Flowgraph T. Lengauer & R. Tarjan, ACM TOPLAS
 * July 1979, pgs 121-141.
 *
 * This implementation runs in time O(n log n). The time bound could be changed to O(n * ack(n))
 * with a small change to the link and eval, and an addition of a child field to the DFS info. In
 * reality, the constant overheads are high enough that the current method is faster in all but the
 * strangest artificially constructed examples.
 *
 * The basic idea behind this algorithm is to perform a DFS walk, keeping track of various info
 * about parents. We then use this info to calculate the dominators, using union-find structures to
 * link together the DFS info, then finally evaluate the union-find results to get the dominators.
 * This implementation is m log n because it does not perform union by rank to keep the union-find
 * tree balanced.
 */
@Constraint(need = {NodeIdMarker.class, NodeListMarker.class})
public final class Dominators<N extends IGraphNode<N>> {
  /* postdom is true if we want post dominators */
  private final boolean postdom;

  private final IGraph<N> graph;

  /* Method's basic blocks. */
  @Nonnull
  private final ImmutableList<N> nodes;

  /** indexed by basic block index */
  @Nonnull
  private final DFSInfo<N>[] info;

  @Nonnull
  private final List<N> vertex;

  /**
   * Constructs an instance.
   *
   * @param graph {@code non-null;} Graph to process
   * @param postdom true for postdom information, false for normal dom info
   */
  @SuppressWarnings("unchecked")
  private Dominators(@Nonnull IGraph<N> graph, boolean postdom) {
    this.graph = graph;
    this.postdom = postdom;
    this.nodes = NodeListMarker.getNodeList(graph);
    this.info = new DFSInfo[nodes.size()]; // Plus implicit entry + exit.
    this.vertex = new ArrayList<N>();
  }

  /**
   * Constructs a fully-initialized instance. (This method exists so as to avoid calling a large
   * amount of code in the constructor.)
   *
   * @param graph {@code non-null;} Control flow graph to process
   * @param postdom true for postdom information, false for normal dom info
   */
  public static <N extends IGraphNode<N>> Dominators<N> make(@Nonnull IGraph<N> graph,
      boolean postdom) {
    Dominators<N> result = new Dominators<N>(graph, postdom);
    result.run();
    return result;
  }

  @Nonnull
  private Iterable<N> getPreds(@Nonnull N block) {
    if (postdom) {
      return block.getSuccessorsIterable();
    } else {
      return block.getPredecessorsIterable();
    }
  }

  @Nonnegative
  private int getIdByNode(@Nonnull N n) {
    return NodeIdMarker.getId(n);
  }

  /**
   * Performs path compress on the DFS info.
   *
   * @param in Basic block whose DFS info we are path compressing.
   */
  private void compress(@Nonnull N in) {
    DFSInfo<N> bbInfo = info[getIdByNode(in)];

    assert bbInfo.ancestor != null;
    DFSInfo<N> ancestorbbInfo = info[getIdByNode(bbInfo.ancestor)];

    if (ancestorbbInfo.ancestor != null) {
      ArrayList<N> worklist = new ArrayList<N>();
      HashSet<N> visited = new HashSet<N>();
      worklist.add(in);

      while (!worklist.isEmpty()) {
        int wsize = worklist.size();
        N v = worklist.get(wsize - 1);
        DFSInfo<N> vbbInfo = info[getIdByNode(v)];
        N vAncestor = vbbInfo.ancestor;
        assert vbbInfo.ancestor != null;
        DFSInfo<N> vabbInfo = info[getIdByNode(vAncestor)];

        // Make sure we process our ancestor before ourselves.
        if (visited.add(vAncestor) && vabbInfo.ancestor != null) {
          worklist.add(vAncestor);
          continue;
        }
        worklist.remove(wsize - 1);

        // Update based on ancestor info.
        if (vabbInfo.ancestor == null) {
          continue;
        }
        N vAncestorRep = vabbInfo.rep;
        N vRep = vbbInfo.rep;
        assert vRep != null;
        if (info[getIdByNode(vAncestorRep)].semidom < info[getIdByNode(vRep)].semidom) {
          vbbInfo.rep = vAncestorRep;
        }
        vbbInfo.ancestor = vabbInfo.ancestor;
      }
    }
  }

  @Nonnull
  private N eval(@Nonnull N v) {
    DFSInfo<N> bbInfo = info[getIdByNode(v)];

    if (bbInfo.ancestor == null) {
      return v;
    }

    compress(v);
    assert bbInfo.rep != null;
    return bbInfo.rep;
  }

  /**
   * Performs dominator/post-dominator calculation for the control flow graph.
   */
  private void run() {
    N root = postdom ? graph.getExitNode() : graph.getEntryNode();

    vertex.add(root);
    DominanceFrontierInfoMarker.setIDom(root, root);

    /*
     * First we perform a DFS numbering of the blocks, by numbering the dfs tree roots.
     */
    DepthFirstTraversal.<N>run(graph, postdom, new DfsWalker());

    // the largest semidom number assigned
    int dfsMax = vertex.size() - 1;

    // Now calculate semidominators.
    for (int i = dfsMax; i >= 2; --i) {
      N w = vertex.get(i);
      DFSInfo<N> wInfo = info[NodeIdMarker.getId(w)];

      for (N predBlock : getPreds(w)) {
        DFSInfo<N> predInfo = info[NodeIdMarker.getId(predBlock)];

        /*
         * PredInfo may not exist in case the predecessor is not reachable.
         */
        if (predInfo != null) {
          int predSemidom = info[NodeIdMarker.getId(eval(predBlock))].semidom;
          if (predSemidom < wInfo.semidom) {
            wInfo.semidom = predSemidom;
          }
        }
      }
      info[NodeIdMarker.getId(vertex.get(wInfo.semidom))].bucket.add(w);

      /*
       * Normally we would call link here, but in our O(m log n) implementation this is equivalent
       * to the following single line.
       */
      wInfo.ancestor = wInfo.parent;

      // Implicity define idom for each vertex.
      ArrayList<N> wParentBucket;
      assert wInfo.parent != null;
      wParentBucket = info[NodeIdMarker.getId(wInfo.parent)].bucket;

      while (!wParentBucket.isEmpty()) {
        int lastItem = wParentBucket.size() - 1;
        N last = wParentBucket.remove(lastItem);
        N u = eval(last);
        DFSInfo<N> uDomInfo = info[NodeIdMarker.getId(u)];
        DFSInfo<N> lastDomInfo = info[NodeIdMarker.getId(last)];
        if (uDomInfo.semidom < lastDomInfo.semidom) {
          DominanceFrontierInfoMarker.setIDom(last, u);
        } else {
          DominanceFrontierInfoMarker.setIDom(last, wInfo.parent);
        }
      }
    }

    // Now explicitly define the immediate dominator of each vertex
    for (int i = 2; i <= dfsMax; ++i) {
      N w = vertex.get(i);
      if (DominanceFrontierInfoMarker.getIDom(w) != vertex
          .get(info[NodeIdMarker.getId(w)].semidom)) {
        DominanceFrontierInfoMarker.setIDom(w,
            DominanceFrontierInfoMarker.getIDom(DominanceFrontierInfoMarker.getIDom(w)));
      }
    }
  }

  /**
   * Callback for depth-first walk through control flow graph (either from the entry block or the
   * exit block). Records the traversal order in the {@code info}list.
   */
  private class DfsWalker extends GraphNodeVisitor<N> {
    @Nonnegative
    private int dfsNum = 0;

    @Override
    public void visitNode(@Nonnull N v, @CheckForNull N parent) {
      if (NodeIdMarker.getId(v) == Integer.MAX_VALUE) {
        return;
      }
      DFSInfo<N> bbInfo = new DFSInfo<N>();
      bbInfo.semidom = ++dfsNum;
      bbInfo.rep = v;
      bbInfo.parent = parent;
      vertex.add(v);
      info[NodeIdMarker.getId(v)] = bbInfo;
    }
  }

  private static final class DFSInfo<N extends IGraphNode<N>> {
    @Nonnegative
    public int semidom;
    @CheckForNull
    public N parent;

    /**
     * rep(resentative) is known as "label" in the paper. It is the node that our block's DFS info
     * has been unioned to.
     */
    @CheckForNull
    public N rep;

    @CheckForNull
    public N ancestor;

    @Nonnull
    public final ArrayList<N> bucket = new ArrayList<N>();
  }
}
