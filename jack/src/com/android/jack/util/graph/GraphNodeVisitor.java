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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Visitor abstract class for graph nodes.
 */
public abstract class GraphNodeVisitor<N extends IGraphNode<N>> {

  /**
   * Visits the current node with a possible parent node.
   *
   * @param v {@code non-null;} graph node visited
   * @param parent {@code null-ok;} parent node if applicable
   */
   public abstract void visitNode(@Nonnull N v, @CheckForNull N parent);
}
