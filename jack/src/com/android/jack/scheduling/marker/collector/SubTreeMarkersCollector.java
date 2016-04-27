/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.scheduling.marker.collector;

import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.marker.Marker;
import com.android.sched.util.config.DefaultFactory;
import com.android.sched.util.config.ReflectDefaultCtorFactory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Collect and cache markers of kind <T>.
 * @param <T> Marker that must be collected.
 */
public class SubTreeMarkersCollector<T extends Marker> {

  @Nonnull
  private final DefaultFactory<? extends SubTreeMarkers<T>> subTreeMarkersFactory;

  @Nonnull
  private final Class<? extends SubTreeMarkers<T>> subTreeMarkersClass;

  private class MarkerCollectorVisitor extends JVisitor {

    @Nonnull
    private final JNode root;

    private MarkerCollectorVisitor(@Nonnull JNode root) {
      this.root = root;
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      SubTreeMarkers<T> collectedMarkers = node.getMarker(subTreeMarkersClass);
      return collectedMarkers == null;
    }

    @Override
    public void endVisit(@Nonnull JNode node) {
      SubTreeMarkers<T> collectedMarkers = node.getMarker(subTreeMarkersClass);

      if (collectedMarkers == null) {
        collectedMarkers = subTreeMarkersFactory.create();
        node.addMarker(collectedMarkers);
      }

      if (!collectedMarkers.isCompletelyAnalyzed()) {
        collectedMarkers.endOfNestedMarkers();

        T markerOfNode = node.getMarker(collectedMarkers.getClassOfCollectedMarkers());
        if (markerOfNode != null) {
          collectedMarkers.addMarker(markerOfNode);
        }
      }

      if (root != node) {
        JNode parentNode = node.getParent();

        if (parentNode != null) {
          List<T> markers = collectedMarkers.getAllMarkers();

          SubTreeMarkers<T> parentCollectedMarkers = parentNode.getMarker(subTreeMarkersClass);
          int beforeMarkerEndPosition;

          if (parentCollectedMarkers == null) {
            parentCollectedMarkers = subTreeMarkersFactory.create();
            parentNode.addMarker(parentCollectedMarkers);
            beforeMarkerEndPosition = 0;
          } else {
            beforeMarkerEndPosition = parentCollectedMarkers.getAllMarkers().size();
          }
          collectedMarkers.setBeforeMarkerEndPosition(beforeMarkerEndPosition);
          collectedMarkers.setAfterMakerStartPosition(beforeMarkerEndPosition + markers.size());
          parentCollectedMarkers.addMarkers(markers);
        }
      }
      super.endVisit(node);
    }

    @Override
    public boolean visit(@Nonnull JIfStatement jIf) {
      super.visit(jIf);
      accept(jIf.getIfExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      super.visit(switchStmt);
      accept(switchStmt.getExpr());
      return false;
    }
  }

  public SubTreeMarkersCollector(@Nonnull Class<? extends SubTreeMarkers<T>> subTreeMarkersClass) {
    this(subTreeMarkersClass, new ReflectDefaultCtorFactory<SubTreeMarkers<T>>(
        subTreeMarkersClass, true));
  }

  public SubTreeMarkersCollector(@Nonnull Class<? extends SubTreeMarkers<T>> subTreeMarkersClass,
      @Nonnull DefaultFactory<? extends SubTreeMarkers<T>> subTreeMarkersFactory) {
    this.subTreeMarkersClass = subTreeMarkersClass;
    this.subTreeMarkersFactory = subTreeMarkersFactory;
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub tree of {@code node}.
   */
  @Nonnull
  public List<T> getSubTreeMarkers(@Nonnull JNode node) {
    return getOrCreateSubTreeMarkers(node).getAllMarkers();
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub trees of {@code node} next
   * sibling.
   */
  @Nonnull
  public List<T> getSubTreeMarkersOnNextSibling(@Nonnull JNode node) {
    JNode parent = node.getParent();
    if (parent == null) {
      return new ArrayList<T>();
    }

    SubTreeMarkers<T> parentCollectedMarkers = getOrCreateSubTreeMarkers(parent);

    SubTreeMarkers<T> collectedMarker = node.getMarker(subTreeMarkersClass);
    assert collectedMarker != null;

    return (parentCollectedMarkers.getAllMarkers().subList(
        collectedMarker.getAfterMarkerStartPosition(),
        parentCollectedMarkers.getPositionOfNestedMarkerEnd()));
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub trees of {@code node}
   * previous sibling.
   */
  @Nonnull
  public List<T> getSubTreeMarkersOnPreviousSibling(@Nonnull JNode node) {
    JNode parent = node.getParent();
    if (parent == null) {
      return new ArrayList<T>();
    }

    List<T> parentCollectedMarkers = getSubTreeMarkers(parent);

    SubTreeMarkers<T> collectedMarker = node.getMarker(subTreeMarkersClass);
    assert collectedMarker != null;

    return (parentCollectedMarkers.subList(0, collectedMarker.getBeforeMarkerEndPosition()));
  }

  /**
   * Collect all markers of kind <T>, that are found on nodes visited before the nodes on the path
   * from {@code subTreeRoot} to {@code endNode}. Considered nodes are those in the subtrees of
   * {@code endNode}'s previous sibling and all of its parents previous siblings up to
   * {@code subTreeRoot}.
   */
  @Nonnull
  public List<T> getMarkersOnNodesLeftToPath(@Nonnull JNode subTreeRoot,
      @Nonnull JNode endNode) {
    getOrCreateSubTreeMarkers(subTreeRoot);

    List<T> markers = new ArrayList<>();

    JNode currentNode = endNode;
    do {
      JNode currentParent = currentNode.getParent();
      SubTreeMarkers<T> collectedMarker = currentNode.getMarker(subTreeMarkersClass);
      assert collectedMarker != null;
      SubTreeMarkers<T> parentCollectedMarkers = currentParent.getMarker(subTreeMarkersClass);
      assert parentCollectedMarkers != null;
      markers.addAll(parentCollectedMarkers.getAllMarkers().subList(
          0, collectedMarker.getBeforeMarkerEndPosition()));
      currentNode = currentParent;
    } while (currentNode != subTreeRoot);

    return markers;
  }

  /**
   * Collect all markers of kind <T>, that are found on nodes visited after the nodes on the path
   * from {@code subTreeRoot} to {@code endNode}. Considered nodes are those in the subtrees of
   * {@code endNode}'s next sibling and all of its parents next siblings up to {@code subTreeRoot}.
   */
  @Nonnull
  public List<T> getMarkersOnNodesRightToPath(@Nonnull JNode subTreeRoot,
      @Nonnull JNode endNode) {
    getOrCreateSubTreeMarkers(subTreeRoot);

    List<T> markers = new ArrayList<>();

    JNode currentNode = endNode;
    do {
      JNode currentParent = currentNode.getParent();
      SubTreeMarkers<T> collectedMarker = currentNode.getMarker(subTreeMarkersClass);
      assert collectedMarker != null;
      SubTreeMarkers<T> parentCollectedMarkers = currentParent.getMarker(subTreeMarkersClass);
      assert parentCollectedMarkers != null;
      markers.addAll(parentCollectedMarkers.getAllMarkers().subList(
          collectedMarker.getAfterMarkerStartPosition(),
          parentCollectedMarkers.getPositionOfNestedMarkerEnd()));
      currentNode = currentParent;
    } while (currentNode != subTreeRoot);

    return markers;
  }

  @Nonnull
  private SubTreeMarkers<T> getOrCreateSubTreeMarkers(@Nonnull JNode node) {
    SubTreeMarkers<T> cm = node.getMarker(subTreeMarkersClass);

    if (cm == null) {
      new MarkerCollectorVisitor(node).accept(node);
      cm = node.getMarker(subTreeMarkersClass);
      assert cm != null;
    }
    return cm;
  }

 }
