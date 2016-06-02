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

import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethodBody;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to insert a {@link JLocal} in the given {@link JMethodBody}
 * and set the {@link JMethodBody} as the {@link JLocal}'s parent.
 */
public class AddJLocalInMethodBody implements TransformationStep, TransformStep {

  @Nonnull
  private final JLocal local;
  @Nonnull
  private final JMethodBody methodBody;

  /**
   * Constructor specifying the {@link JLocal} to add into the {@link JMethodBody}.
   *
   * @param local the local variable to add to the method body
   * @param methodBody the body of the method to update
   */
  public AddJLocalInMethodBody(@Nonnull JLocal local, @Nonnull JMethodBody methodBody) {
    this.local = local;
    this.methodBody = methodBody;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    methodBody.addLocal(local);
    local.updateParents(methodBody);
  }

}
