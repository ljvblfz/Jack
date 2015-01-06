/*
 * Copyright 2007 Google Inc.
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
package com.android.jack.ir;

import com.android.jack.ir.sourceinfo.SourceInfo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link Error} related to an IR node that is not supposed not happen.
 */
public class JNodeInternalError extends Error {

  private static final long serialVersionUID = 1L;

  /**
   * Information regarding a node that was being processed when a {@link JNodeInternalError} was
   * thrown.
   */
  public static final class NodeInfo {

    static void preload() {
      // Initialize this class on static invocation.
    }

    @Nonnull
    private final String className;
    @Nonnull
    private final String description;
    @CheckForNull
    private final SourceInfo sourceInfo;

    NodeInfo(@Nonnull String className, @Nonnull String description,
        @CheckForNull SourceInfo sourceInfo) {
      this.className = className;
      this.description = description;
      this.sourceInfo = sourceInfo;
    }

    /**
     * Returns the name of the Java class of the node.
     */
    @Nonnull
    public String getClassName() {
      return className;
    }

    /**
     * Returns a text description of the node; typically toString().
     */
    @Nonnull
    public String getDescription() {
      return description;
    }

    /**
     * Returns the node's source info, if available; otherwise {@code null}.
     */
    @CheckForNull
    public SourceInfo getSourceInfo() {
      return sourceInfo;
    }
  }

  /**
   * Tracks if there's a pending addNode() to avoid recursion sickness.
   */
  @Nonnull
  private static final ThreadLocal<JNodeInternalError> pendingICE =
      new ThreadLocal<JNodeInternalError>();

  /**
   * Force this class to be preloaded. If we don't preload this class, we can
   * get into bad behavior if we later try to load this class under out of
   * memory or out of stack conditions.
   */
  public static void preload() {
    // Initialize this class on static invocation.
    NodeInfo.preload();
    pendingICE.set(pendingICE.get());
  }

  @Nonnull
  private final List<NodeInfo> nodeTrace = new ArrayList<NodeInfo>();

  /**
   * Constructs a new exception with the specified node and message.
   */
  public JNodeInternalError(@Nonnull HasSourceInfo node, @Nonnull String message) {
    super(message);
    addNode(node);
  }

  /**
   * Constructs a new exception with the specified cause.
   */
  public JNodeInternalError(@Nonnull Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   */
  public JNodeInternalError(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    String message = super.getMessage();
    if (nodeTrace.isEmpty()) {
      return message;
    } else {
      NodeInfo bottomNode = nodeTrace.get(0);
      return message + ": " + bottomNode.getClassName() + " at \""
          + bottomNode.getSourceInfo() + '"';
    }
  }

  /**
   * Adds a node to the end of the node trace. This is similar to how a stack
   * trace works.
   */
  public void addNode(@Nonnull HasSourceInfo node) {
    JNodeInternalError other = pendingICE.get();
    if (other != null) {
      // Avoiding recursion sickness: Yet Another ICE must have occurred while
      // generating info for a prior ICE. Just bail!
      return;
    }

    String className = null;
    String description = null;
    SourceInfo sourceInfo = null;
    try {
      pendingICE.set(this);
      className = node.getClass().getName();
      sourceInfo = node.getSourceInfo();
      description = node.toString();
    } catch (Throwable e) {
      // ignore any exceptions
      if (description == null) {
        description = "<source info not available>";
      }
    } finally {
      pendingICE.set(null);
    }
    assert className != null;
    addNode(className, description, sourceInfo);
  }

  /**
   * Adds information about a a node to the end of the node trace.
   * This is similar to how a stack trace works.
   */
  public void addNode(@Nonnull String className, @Nonnull String description,
      @CheckForNull SourceInfo sourceInfo) {
    nodeTrace.add(new NodeInfo(className, description, sourceInfo));
  }

  /**
   * Returns a list of nodes that were being processed when this exception was
   * thrown. The list reflects the parent-child relationships of the AST and is
   * is in order from children to parents. The first element of the returned
   * list is the node that was most specifically being visited when the
   * exception was thrown.
   */
  @Nonnull
  public List<NodeInfo> getNodeTrace() {
    return nodeTrace;
  }

}
