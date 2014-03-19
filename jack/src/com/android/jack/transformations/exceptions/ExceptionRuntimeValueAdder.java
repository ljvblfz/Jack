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

package com.android.jack.transformations.exceptions;

import com.android.jack.Options;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAsgOperation.NonReusedAsg;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Add JExceptionRuntimeValue expression.
 */
@Description("Add JExceptionRuntimeValue expression")
@Name("ExceptionRuntimeValueAdder")
@Constraint(need = {JCatchBlock.class}, no = {JTryStatement.FinallyBlock.class})
@Transform(
    add = {JExceptionRuntimeValue.class, JLocalRef.class, NonReusedAsg.class})
public class ExceptionRuntimeValueAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    private Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JCatchBlock jCatchBlock) {
      SourceInfo sourceInfo = jCatchBlock.getSourceInfo();
      JLocalRef localRef = new JLocalRef(sourceInfo, jCatchBlock.getCatchVar());
      JAsgOperation assign =
          new JAsgOperation(sourceInfo, localRef, new JExceptionRuntimeValue(sourceInfo,
              (JClassOrInterface) localRef.getType()));
      tr.append(new PrependStatement(jCatchBlock, assign.makeStatement()));
      return super.visit(jCatchBlock);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor rca = new Visitor(tr);
    rca.accept(method);
    tr.commit();
  }
}
