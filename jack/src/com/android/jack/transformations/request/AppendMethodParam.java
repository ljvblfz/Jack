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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to add a {@link JParameter}
 * as the last parameter of a {@link JMethod}.
 *
 * @see PrependMethodParam
 */
public class AppendMethodParam implements TransformationStep, TransformStep {

  @Nonnull
  private final JMethod method;

  @Nonnull
  private final JParameter parameter;

  /**
   * Constructor specifying the {@code parameter} to add as the last parameter of
   * the given {@code method}.
   *
   * @param method the method to update
   * @param parameter the parameter to add to the method
   */
  public AppendMethodParam(@Nonnull JMethod method, @Nonnull JParameter parameter) {
    this.method = method;
    this.parameter = parameter;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    assert method.getParams().size() == method.getMethodIdWide().getParamTypes().size();
    method.addParam(parameter);
    method.getMethodIdWide().addParam(parameter.getType());
    parameter.updateParents(method);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Append parameter ");
    sb.append(parameter.toString());
    sb.append(" in ");
    sb.append(method.toString());
    return sb.toString();
  }
}
