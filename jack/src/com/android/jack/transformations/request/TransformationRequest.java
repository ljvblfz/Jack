/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * List of transformations that can be applied on the JAST.
 */
public class TransformationRequest {

  @CheckForNull
  private List<TransformationStep> requests = new LinkedList<TransformationStep>();

  /**
   * Constructor specifying the root node of the transformations. The root node should be the most
   * common parent to the transformations.
   *
   * Here are some examples of root nodes:
   * <ul>
   * <li>When transformations apply on code, the root node should be the {@link JMethod} that is
   * affected by these transformations.</li>
   * <li>When adding fields or methods to a class, the root node should be the
   * {@link JDefinedClassOrInterface} that is affected.</li>
   * </ul>
   *
   * @param root a non-null {@link JNode}
   */
  public TransformationRequest(@Nonnull JNode root) {
    assert root != null;
  }

  /**
   * Appends a {@link TransformationStep} to this request.
   *
   * @param step a transformation step
   * @throws IllegalStateException if the request has already been applied with {@link #commit}
   */
  public void append(@Nonnull TransformationStep step) {
    if (requests == null) {
      throw new IllegalStateException("The request has already been applied");
    }
    assert requests != null;
    requests.add(step);
  }

  /**
   * Appends a collection of {@link TransformationStep} to this request.
   *
   * @param steps a collection of transformation steps
   * @throws IllegalStateException if the request has already been applied with {@link #commit}
   */
  public void appendAll(Collection<TransformationStep> steps) {
    if (requests == null) {
      throw new IllegalStateException("The request has already been applied");
    }
    assert requests != null;
    requests.addAll(steps);
  }

  /**
   * Applies each {@link TransformationStep} added to this transformation request. The
   * transformation steps are applied in the order that they have been added. After the
   * commit, the transformation request cannot be used.
   *
   * @throws UnsupportedOperationException if a transformation cannot be applied.
   */
  public void commit() throws UnsupportedOperationException {
    if (requests == null) {
      throw new IllegalStateException("The same request cannot be applied twice");
    }

    assert requests != null;
    for (TransformationStep step : requests) {
      step.apply();
    }

    // any further attempt to use this Request must crash.
    requests = null;
  }

}
