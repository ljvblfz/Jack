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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to add a {@link JMethod}
 * as the last method of a {@link JDefinedClassOrInterface}.
 */
public class AppendMethod implements TransformationStep, TransformStep {

  @Nonnull
  private final JDefinedClassOrInterface type;

  @Nonnull
  private final JMethod method;

  /**
   * Constructor specifying the {@code method} to add to the given {@code type}.
   *
   * @param type the class or interface to update
   * @param method the method to add to the class or interface
   */
  public AppendMethod(@Nonnull JDefinedClassOrInterface type, @Nonnull JMethod method) {
    this.type = type;
    this.method = method;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    type.addMethod(method);
    method.updateParents(type);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Append ");
    sb.append(method.toString());
    sb.append(" in ");
    sb.append(type.toString());
    return sb.toString();
  }
}
