/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.cfg;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JStatement;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link CatchBasicBlock} is a special block to represent catch block into the control flow graph.
 * {@link CatchBasicBlock} will be targeted {@code PeiBasicBlock}.
 */
public class CatchBasicBlock extends NormalBasicBlock {

  @Nonnull
  private final List<JClass> catchTypes;

  @Nonnull
  private final JLocal catchVar;

  public CatchBasicBlock(@Nonnull ControlFlowGraph cfg, @Nonnull List<JStatement> statements,
      @Nonnull List<JClass> catchTypes, @Nonnull JLocal catchVar) {
    super(cfg, statements);
    this.catchTypes = catchTypes;
    this.catchVar = catchVar;
  }

  @Nonnull
  public List<JClass> getCatchTypes() {
    return catchTypes;
  }

  @Nonnull
  public JLocal getCatchVar() {
    return catchVar;
  }
}
