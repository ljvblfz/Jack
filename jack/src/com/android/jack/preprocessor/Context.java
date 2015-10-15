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

import com.android.jack.ir.ast.JAnnotationType;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Common context for evaluation of expressions.
 */
public class Context {
  @Nonnull
  protected final Rule rule;

  @Nonnull
  final Collection<AddAnnotationStep> steps =
      new ArrayList<AddAnnotationStep>();

  public Context(@Nonnull Rule rule) {
    this.rule = rule;
  }

  public void addAnnotate(@Nonnull JAnnotationType toAdd, @Nonnull Collection<?> collection) {
    steps.add(new AddAnnotationStep(rule, toAdd, collection));
  }

  public void addAnnotate(@Nonnull AddAnnotationStep toAdd) {
    steps.add(toAdd);
  }

  @Nonnull
  public Collection<AddAnnotationStep> getSteps() {
    return steps;
  }
}
