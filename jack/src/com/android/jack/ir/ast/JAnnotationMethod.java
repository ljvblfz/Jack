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
import com.android.jack.load.MethodLoader;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Method of an annotation type.
 */
@Description("Method of an annotation type")
public class JAnnotationMethod extends JMethod {

  private static final long serialVersionUID = 1L;

  @CheckForNull
  private JLiteral defaultValue;

  public JAnnotationMethod(
      @Nonnull SourceInfo info,
      @Nonnull JMethodId id,
      @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JType returnType,
      int modifier) {
    super(info, id, enclosingType, returnType, modifier);
  }

  public JAnnotationMethod(
      @Nonnull SourceInfo info,
      @Nonnull JMethodId id,
      @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JType returnType,
      int modifier,
      @Nonnull MethodLoader loader) {
    super(info, id, enclosingType, returnType, modifier, loader);
  }

  public void setDefaultValue(@CheckForNull JLiteral defaultValue) {
    this.defaultValue = defaultValue;
  }

  @CheckForNull
  public JLiteral getDefaultValue() {
    return defaultValue;
  }

  @Override
  protected void visitChildren(@Nonnull JVisitor visitor) {
    super.visitChildren(visitor);
    if (defaultValue != null) {
      visitor.accept(defaultValue);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (existingNode == defaultValue) {
      defaultValue = null;
    } else {
      super.removeImpl(existingNode);
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    if (existingNode == defaultValue) {
      defaultValue = (JLiteral) newNode;
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
