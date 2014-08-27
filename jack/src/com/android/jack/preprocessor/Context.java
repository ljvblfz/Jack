/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.JNode;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Common context for evaluation of expressions.
 */
public class Context {

  @Nonnull
  final Collection<TransformationStep> steps =
      new ArrayList<TransformationStep>();

  public void addAnnotate(@Nonnull TransformationStep toAdd) {
    steps.add(toAdd);
  }

  @Nonnull
  public TransformationRequest getRequest(@Nonnull JNode root) {
    TransformationRequest request = new TransformationRequest(root);
    request.appendAll(steps);
    return request;
  }
  @Nonnull
  public Collection<TransformationStep> getSteps() {
    return steps;
  }
}
