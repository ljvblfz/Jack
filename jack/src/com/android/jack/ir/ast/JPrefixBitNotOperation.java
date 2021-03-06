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
package com.android.jack.ir.ast;

import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Unary operator expression for {@code prefix bit not}.
 */
@Description("Bit not prefix operator expression")
public class JPrefixBitNotOperation extends JPrefixOperation {

  public JPrefixBitNotOperation(@Nonnull SourceInfo info, @Nonnull JExpression arg) {
    super(info, arg);
  }

  @Override
  @Nonnull
  public JUnaryOperator getOp() {
    return JUnaryOperator.BIT_NOT;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  @Nonnull
  // Section Bitwise Complement Operator ~ (JLS-7 15.15.5)
  public JType getType() {
    JType argType = getArg().getType();
    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(argType);
    return (JPrimitiveType.getUnaryPromotion(argType));
  }
}
