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
import com.android.jack.ir.ast.JField;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to add a {@link JField}
 * as the last field of a {@link JDefinedClassOrInterface}.
 */
public class AppendField implements TransformationStep, TransformStep {

  @Nonnull
  private final JDefinedClassOrInterface type;

  @Nonnull
  private final JField field;

  /**
   * Constructor specifying the {@code field} to add to the given {@code type}.
   *
   * @param type the class or interface to update
   * @param field the field to add to the class or interface
   */
  public AppendField(@Nonnull JDefinedClassOrInterface type, @Nonnull JField field) {
    this.type = type;
    this.field = field;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    type.addField(field);
    field.updateParents(type);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Append ");
    sb.append(field.toString());
    sb.append(" in ");
    sb.append(type.toString());
    return sb.toString();
  }
}
