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

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Cast expression.
 */
@Description("Cast expression")
public abstract class JCastOperation extends JExpression {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private JType castType;

  @Nonnull
  protected JExpression expr;

  public JCastOperation(@Nonnull SourceInfo info, @Nonnull JType castType,
      @Nonnull JExpression expr) {
    super(info);
    this.castType = castType;
    this.expr = expr;
  }

  @Nonnull
  public JType getCastType() {
    return castType;
  }

  @Nonnull
  public JExpression getExpr() {
    return expr;
  }

  @Override
  @Nonnull
  public JType getType() {
    return castType;
  }

  public void setType(@Nonnull JType type) {
    this.castType = type;
  }

  /**
   * Resolve an external reference during AST stitching.
   */
  public void resolve(@Nonnull JType newType) {
    castType = newType;
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (expr == existingNode) {
      expr = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
