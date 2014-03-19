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

package com.android.jack.ecj.loader.jast;

import com.android.jack.ir.ast.JDefinedClassOrInterface;

import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

import javax.annotation.Nonnull;

/**
 * One {@code IBinaryNestedType} loaded from a J-AST element.
 */
class JAstBinaryNestedType implements IBinaryNestedType {

  @Nonnull
  private final JDefinedClassOrInterface nestedType;

  /**
   * Creates a nested type.
   */
  JAstBinaryNestedType(JDefinedClassOrInterface nestedType) {
    this.nestedType = nestedType;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getName() {
    return LoaderUtils.getQualifiedNameFormatter().getName(nestedType).toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getModifiers() {
    throw new AssertionError("Not Yet Implemented");
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getEnclosingTypeName() {
    return LoaderUtils.getQualifiedNameFormatter()
        .getName(nestedType.getEnclosingType()).toCharArray();
  }
}
