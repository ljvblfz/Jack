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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code assign mod}.
 */
@Description("Assign mod operator expression")
public class JAsgModOperation extends JAsgBinaryOperation {

  public JAsgModOperation(
      @Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  @Nonnull
  public JBinaryOperator getOp() {
    return JBinaryOperator.ASG_MOD;
  }

  @Override
  public boolean canThrow() {
    return (getType() == JPrimitiveTypeEnum.BYTE.getType())
        || (getType() == JPrimitiveTypeEnum.CHAR.getType())
        || (getType() == JPrimitiveTypeEnum.SHORT.getType())
        || (getType() == JPrimitiveTypeEnum.INT.getType())
        || (getType() == JPrimitiveTypeEnum.LONG.getType());
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
