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

package com.android.jack.jayce.linker;

import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;

import javax.annotation.Nonnull;

/**
 * {@code Linker} for {@link JVariable}.
 */
public class VariableRefLinker implements Linker<JVariable> {

  @Nonnull
  private final JVariableRef varRef;

  public VariableRefLinker(@Nonnull JVariableRef varRef) {
    this.varRef = varRef;
  }

  @Override
  public void link(@Nonnull JVariable resolvedTarget) {
    varRef.setTarget(resolvedTarget);
  }
}