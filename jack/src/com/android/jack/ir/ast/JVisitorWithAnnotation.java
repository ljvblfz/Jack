/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * A visitor for iterating through an AST that also visit annotations.
 */
public class JVisitorWithAnnotation extends JVisitor {

  protected JVisitorWithAnnotation() {
    this(/* needLoading =*/ true);
  }

  protected JVisitorWithAnnotation(boolean needLoading) {
    super(needLoading);
  }

  @Override
  public void endVisit(@Nonnull JAnnotation annotation) {
    endVisit((JExpression) annotation);
  }

  @Override
  public boolean visit(@Nonnull JAnnotation annotation) {
    return visit((JExpression) annotation);
  }

  @Override
  public void visit(@Nonnull JAnnotation annotation, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) annotation, transformRequest);
  }
}
