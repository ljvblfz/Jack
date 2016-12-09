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

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JInterface;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} to add implements of {@link JInterface}
 * to {@link JDefinedClass}.
 */
public class AddImplements implements TransformationStep, TransformStep {

  @Nonnull
  private final JDefinedClass definedClass;

  @Nonnull
  private final JInterface iface;

  /**
   * Constructor specifying a class that will implement an interface.
   *
   * @param definedClass the defined class that will implement the interface
   * @param iface the interface to be implemented by the defined class
   */
  public AddImplements(@Nonnull JDefinedClass definedClass, @Nonnull JInterface iface) {
    this.definedClass = definedClass;
    this.iface = iface;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    definedClass.addImplements(iface);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("add implements of ");
    sb.append(iface.toString());
    sb.append(" to ");
    sb.append(definedClass.toString());
    return sb.toString();
  }
}
