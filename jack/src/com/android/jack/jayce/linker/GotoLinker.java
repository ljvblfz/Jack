/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabeledStatement;

import javax.annotation.Nonnull;

/**
 * {@code Linker} for {@link JGoto}.
 */
public class GotoLinker implements Linker<JLabeledStatement> {
  @Nonnull
  private final JGoto jGoto;

  public GotoLinker(@Nonnull JGoto jGoto) {
    this.jGoto = jGoto;
  }

  @Override
  public void link(@Nonnull JLabeledStatement resolvedTarget) {
    jGoto.setTarget(resolvedTarget);
  }
}