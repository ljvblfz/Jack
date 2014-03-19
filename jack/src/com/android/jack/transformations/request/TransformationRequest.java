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

import com.android.jack.ir.ast.JNode;

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

  public TransformationRequest(@Nonnull JNode root) {
    assert root != null;
  }

  public void append(@Nonnull TransformationStep step) {
    if (requests == null) {
      throw new IllegalStateException("The request has already been applied");
    }
    assert requests != null;
    requests.add(step);
  }

  /**
   * @Throws {@link UnsupportedOperationException} if a transformation can not be applied.
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
