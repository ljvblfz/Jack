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

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethodCall;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to add a {@code JExpression}
 * as the last argument of a {@code JMethodCall}
 */
public class AppendArgument implements TransformationStep, TransformStep {

  @Nonnull
  private final JMethodCall methodCall;

  @Nonnull
  private final JExpression argument;

  public AppendArgument(@Nonnull JMethodCall methodCall, @Nonnull JExpression argument) {
    this.methodCall = methodCall;
    this.argument = argument;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    methodCall.addArg(argument);
    argument.updateParents(methodCall);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Append argument ");
    sb.append(argument.toString());
    sb.append(" in ");
    sb.append(methodCall.toString());
    return sb.toString();
  }
}
