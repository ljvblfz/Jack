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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/** Implements series of variable related utilities. */
public final class CfgVarUtils {
  /** Defines sets of transformations for method replaceWithLocal(...) */
  @Transform(add = { JAsgOperation.NonReusedAsg.class, JLocalRef.class },
      modify = JControlFlowGraph.class)
  @Use(LocalVarCreator.class)
  public static class ReplaceWithLocal {
  }

  /**
   * Replaces the non-throwing subexpression with a newly created local.
   *
   * Reuses EH context from the current block element.
   */
  @Nonnull
  public JLocalRef replaceWithLocal(
      @Nonnull LocalVarCreator varCreator, @Nonnull JExpression expr) {
    assert !expr.canThrow(); // Non-throwing expression only
    SourceInfo srcInfo = expr.getSourceInfo();

    // Get outer block element and basic block
    JBasicBlockElement element = expr.getParent(JBasicBlockElement.class);
    JBasicBlock block = element.getBasicBlock();
    int index = block.indexOf(element);

    // Create a new local
    TransformationRequest request = new TransformationRequest(block.getCfg().getMethod());
    JLocal tmp = varCreator.createTempLocal(expr.getType(), SourceInfo.UNKNOWN, request);

    // Replace the expression with local reference
    JLocalRef result = tmp.makeRef(srcInfo);
    request.append(new Replace(expr, result));

    // Commit the operation to apply changes, not that the following operation
    // resets `expr`s parent
    request.commit();

    // Create and insert the local initialization
    block.insertElement(index,
        new JVariableAsgBlockElement(srcInfo,
            element.getEHContext(), new JAsgOperation(srcInfo, tmp.makeRef(srcInfo), expr)));

    return result;
  }
}
