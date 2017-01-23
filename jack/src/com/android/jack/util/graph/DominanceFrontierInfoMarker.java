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

import com.android.jack.dx.util.BitIntSet;
import com.android.jack.dx.util.IntSet;
import com.android.jack.dx.util.ListIntSet;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A marker to store information computed by {@Link DominanceFrontier}.
 */
@Description("Dominance Frontier information for a node in a CFG.")
@ValidOn(JBasicBlock.class)
public class DominanceFrontierInfoMarker implements Marker {
  /**
   * BitIntSet/ListIntSet threshold for dominance frontier sets. These sets are kept per basic block
   * until phi placement and tend to be, like the CFG itself, very sparse at large sizes.
   *
   * A value of 3072 here is somewhere around 1.125mb of total bitset size.
   */
  private static final int DOMFRONT_SET_THRESHOLD_SIZE = 3072;

  @Nonnull
  public final IntSet dominanceFrontiers;

  /** {@code >= 0 after run();} the index of the immediate dominator */
  @CheckForNull
  public IGraphNode<?> idom = null;

  private DominanceFrontierInfoMarker(@Nonnull int szBlocks) {
    dominanceFrontiers =
        szBlocks <= DOMFRONT_SET_THRESHOLD_SIZE ? new BitIntSet(szBlocks) : new ListIntSet();
  }

  @Nonnull
  public static DominanceFrontierInfoMarker getDomInfo(JBasicBlock bb) {
    DominanceFrontierInfoMarker marker = bb.getMarker(DominanceFrontierInfoMarker.class);
    assert marker != null;
    return marker;
  }

  public static <N extends IGraphNode<N>> void setDomInfo(N node, int size) {
    DominanceFrontierInfoMarker marker = new DominanceFrontierInfoMarker(size);
    node.addMarker(marker);
  }

  public static <N extends IGraphNode<N>> void setIDom(N parent, N idom) {
    DominanceFrontierInfoMarker marker = parent.getMarker(DominanceFrontierInfoMarker.class);
    assert marker != null;
    marker.idom = idom;
  }

  @SuppressWarnings("unchecked")
  public static <N extends IGraphNode<N>> N getIDom(N parent) {
    DominanceFrontierInfoMarker marker = parent.getMarker(DominanceFrontierInfoMarker.class);
    assert marker != null;
    return (N) marker.idom;
  }

  public static <N extends IGraphNode<N>> boolean isInDominanceFrontier(N parent, N target) {
    DominanceFrontierInfoMarker df = parent.getMarker(DominanceFrontierInfoMarker.class);
    assert df != null;
    int targetIdx = NodeIdMarker.getId(target);
    assert df.dominanceFrontiers != null;
    return df.dominanceFrontiers.has(targetIdx);
  }

  public static <N extends IGraphNode<N>> void addDominanceFrontier(N parent, N target) {
    DominanceFrontierInfoMarker df = parent.getMarker(DominanceFrontierInfoMarker.class);
    assert df != null;
    int targetIdx = NodeIdMarker.getId(target);
    assert df.dominanceFrontiers != null;
    df.dominanceFrontiers.add(targetIdx);
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }

  /**
   * Removes all dominance frontier info marker from a graph.
   */
  public static void clearMarkers(@Nonnull IGraph<?> graph) {
    for (IGraphNode<?> n : graph.getNodes()) {
      n.removeMarker(DominanceFrontierInfoMarker.class);
    }
  }
}
