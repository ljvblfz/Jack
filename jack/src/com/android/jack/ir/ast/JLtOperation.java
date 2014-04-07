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

package com.android.jack.ir.ast;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code lt}.
 */
@Description("Lt operator expression")
public class JLtOperation extends JRelationalOperation {

  public JLtOperation(
      @Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  @Nonnull
  public JBinaryOperator getOp() {
    return JBinaryOperator.LT;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
