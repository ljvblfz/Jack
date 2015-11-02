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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to change the superclass of a
 * a {@code JDefinedClass} to be a {@code JClass}.
 */
public class ChangeSuperClass implements TransformationStep, TransformStep {

  @Nonnull
  private final JDefinedClass definedClass;

  @Nonnull
  private final JClass superClass;

  public ChangeSuperClass(@Nonnull JDefinedClass definedClass, @Nonnull JClass superClass) {
    this.definedClass = definedClass;
    this.superClass = superClass;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    definedClass.setSuperClass(superClass);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Change SuperClass of ");
    sb.append(definedClass.toString());
    sb.append(" to ");
    sb.append(superClass.toString());
    return sb.toString();
  }
}
