/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;


import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.impl.SourceGenerationVisitor;
import com.android.jack.ir.impl.ToStringGenerationVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.marker.collector.SubTreeMarkersCollector;
import com.android.jack.util.DefaultTextOutput;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.marker.Marker;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for all visitable AST nodes.
 */
@Description("AST Node")
public abstract class JNode extends LocalMarkerManager
  implements JVisitable, HasSourceInfo, Component {

  /**
   * Transformation kind.
   */
  protected static enum Transformation {
    REMOVE,
    REPLACE,
    INSERT_BEFORE,
    INSERT_AFTER;
  }

  private static class ParentSetterVisitor extends JVisitor {

    final Stack<JNode> nodes = new Stack<JNode>();


    private ParentSetterVisitor(@Nonnull JNode initialParent) {
      super(false /* needLoading */);
      assert initialParent != null;
      nodes.push(initialParent);
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      assert !nodes.isEmpty();
      JNode newParent = nodes.peek();

      // endVisit always pop, so we need to always push, even when visit is interrupted
      nodes.push(node);

      if (node.getParent() == newParent) {
        // if parent is already correctly set, this mean all childs of the node already have their
        // parent set correctly, so we can just cut the visit there.
        return false;
      }

      node.setParent(newParent);
      return super.visit(node);
    }

    @Override
    public boolean visit(@Nonnull JLambda lambda) {
      boolean visitChild = super.visit(lambda);
      if (visitChild) {
        accept(lambda.getMethod());
      }
      return visitChild;
    }

    @Override
    public void endVisit(@Nonnull JNode node) {
      nodes.pop();
      super.endVisit(node);
    }
  }

  @Nonnull
  protected SourceInfo info;

  protected JNode parent = null;

  protected JNode(@Nonnull SourceInfo info) {
    assert info != null : "SourceInfo must be provided for JNodes";
    this.info = info;
  }

  /**
   * @return the parent
   */
  public JNode getParent() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends JNode> T getParent(@Nonnull Class<T> classToSearch) {
    JNode result = getParent();

    while (result != null && !(classToSearch.isAssignableFrom(result.getClass()))) {
      result = result.getParent();
    }

    if (result == null) {
      throw new NoSuchElementException();
    }

    return (T) result;
  }

  @Nonnull
  @Override
  public SourceInfo getSourceInfo() {
    return info;
  }

  public void setSourceInfo(@Nonnull SourceInfo info) {
    this.info = info;
  }

  // Causes source generation to delegate to the one visitor
  public final String toSource() {
    DefaultTextOutput out = new DefaultTextOutput(false);
    SourceGenerationVisitor v = new SourceGenerationVisitor(out);
    v.accept(this);
    return out.toString();
  }

  // Causes source generation to delegate to the one visitor
  @Override
  public final String toString() {
    String str;

    DefaultTextOutput out = new DefaultTextOutput(true);
    ToStringGenerationVisitor v = new ToStringGenerationVisitor(out);
    v.accept(this);
    str = out.toString();

    SourceInfo sourceInfo = getSourceInfo();
    if (sourceInfo != SourceInfo.UNKNOWN) {
      str += " (" + getSourceInfo().toString() + ")";
    }

    return str;
  }

  public final void remove(@Nonnull JNode existingNode)
      throws UnsupportedOperationException, ClassCastException {
    removeImpl(existingNode);
  }

  public final void replace(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException, ClassCastException {
    replaceImpl(existingNode, newNode);
    newNode.updateParents(this);
  }

  public final void insertBefore(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException, ClassCastException {
    insertBeforeImpl(existingNode, newNode);
    newNode.updateParents(this);
  }

  public final void insertAfter(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException, ClassCastException {
    insertAfterImpl(existingNode, newNode);
    newNode.updateParents(this);
  }

  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException  {
    transform(existingNode, null, Transformation.REMOVE);
  }

  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException  {
    transform(existingNode, newNode, Transformation.REPLACE);
  }

  protected void insertBeforeImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException  {
    transform(existingNode, newNode, Transformation.INSERT_BEFORE);
  }

  protected void insertAfterImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException  {
    transform(existingNode, newNode, Transformation.INSERT_AFTER);
  }

  /**
   * Transform a {@link JNode}.
   *
   * @param existingNode {@link JNode} to transform.
   * @param newNode {@link JNode} that will replace the existing node if the transformation kind is
   *        set to remove.
   * @param transformation transformation kind.
   * @throws UnsupportedOperationException
   */
 protected void transform(
      @Nonnull JNode existingNode,
      @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException  {
    throw new UnsupportedOperationException(getClass().getName()
        + " does not support transformation '" + transformation.name()
        + "', existing: " + existingNode.getClass().getName()
        + ", new:  "
        + (newNode == null ? "<null>" : newNode.getClass().getName()));
  }

  protected static <T> boolean transform(
      @Nonnull List<T> list,
      @Nonnull JNode existingNode,
      @CheckForNull T newNode,
      @Nonnull Transformation transformation) {
    assert existingNode != null;

    int indexOfExisting = list.indexOf(existingNode);
    if (indexOfExisting != -1) {
      // TODO(jmhenaff): Rethink how this is done eventually.
      // The fact that Jack uses Lists lead to this implementation.
      switch (transformation) {
        case INSERT_AFTER:
          assert newNode != null;
          list.add(indexOfExisting + 1, newNode);
          break;
        case INSERT_BEFORE:
          assert newNode != null;
          list.add(indexOfExisting, newNode);
          break;
        case REPLACE:
          assert newNode != null;
          list.set(indexOfExisting, newNode);
          break;
        case REMOVE:
          assert newNode == null;
          list.remove(indexOfExisting);
          break;
        default :
          throw new AssertionError();
      }
      return true;
    }

    return false;
  }

  public void updateParents(JNode parent) {
    new ParentSetterVisitor(parent).accept(this);
  }

  /**
   * @param parent the parent to set
   */
  private void setParent(JNode parent) {
    assert parent != null;
    this.parent = parent;
  }

  /**
   * Check if the result of an expression is used or not.
   * @param expr {@link JExpression} that we want to check the result usage.
   * @return true if result of {@link JExpression} is used, false otherwise.
   */
  protected boolean isResultOfExpressionUsed(JExpression expr) {
    throw new AssertionError("Not yet supported");
  }

  public boolean canThrow() {
    return false;
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub tree of {@code node}.
   */
  @Nonnull
  public <T extends Marker> List<T> getSubTreeMarkers(
      @Nonnull SubTreeMarkersCollector<T> collector) {
    return collector.getSubTreeMarkers(this);
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub trees of {@code node} next
   * sibling.
   */
  @Nonnull
  public <T extends Marker> List<T> getSubTreeMarkersOnNextSibling(
      @Nonnull SubTreeMarkersCollector<T> collector) {
    return collector.getSubTreeMarkersOnNextSibling(this);
  }

  /**
   * Collect all markers of kind <T>, that are accessible from sub trees of {@code node}
   * previous sibling.
   */
  @Nonnull
  public <T extends Marker> List<T> getSubTreeMarkersOnPreviousSibling(
      @Nonnull SubTreeMarkersCollector<T> collector) {
    return collector.getSubTreeMarkersOnPreviousSibling(this);
  }

  public abstract void checkValidity();
}
