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

import com.android.jack.ir.ast.JDefinedClass;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to remove the enclosing method of a
 * {@link JDefinedClass}.
 */
public class RemoveEnclosingMethod implements TransformationStep {

  @Nonnull
  private final JDefinedClass type;

  public RemoveEnclosingMethod(@Nonnull JDefinedClass type) {
    this.type = type;
  }

  @Override
  public void apply() {
    type.setEnclosingMethod(null);
  }

}
