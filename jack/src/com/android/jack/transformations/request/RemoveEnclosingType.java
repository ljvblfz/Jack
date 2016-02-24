/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to remove the enclosing type of a
 * {@link JDefinedClassOrInterface}.
 */
public class RemoveEnclosingType implements TransformationStep {

  @Nonnull
  private final JDefinedClassOrInterface type;

  public RemoveEnclosingType(@Nonnull JDefinedClassOrInterface type) {
    this.type = type;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    JClassOrInterface removedEnclosing = type.getEnclosingType();
    type.setEnclosingType(null);
    if (removedEnclosing instanceof JDefinedClassOrInterface) {
      ((JDefinedClassOrInterface) removedEnclosing).removeMemberType(type);
    }
  }

}
