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

package com.android.jack.transformations.ast.removeinit;

import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.CloneStatementVisitor;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * Remove call to method initializing field values.
 */
@Description("Remove call to method initializing field values.")
@Name("FieldInitMethodCallRemover")
@Constraint(need = {JMethodCall.class, FieldInitMethod.class, OriginalNames.class},
no = JFieldInitializer.class)
@Transform(remove = {FieldInitMethodCall.class, ThreeAddressCodeForm.class})
@Use(CloneStatementVisitor.class)
@Filter(SourceTypeFilter.class)
// This schedulable clones the body of methods it does not visit.
@ExclusiveAccess(JDefinedClassOrInterface.class)
public class FieldInitMethodCallRemover implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JDefinedClassOrInterface declaredType;

    public Visitor(@Nonnull TransformationRequest tr,
        @Nonnull JDefinedClassOrInterface declaredType) {
      this.tr = tr;
      this.declaredType = declaredType;
    }

    @Override
    public void endVisit(@Nonnull JMethodCall methodCall) {
      if (methodCall.getMethodName().equals(FieldInitMethodRemover.VAR_INIT_METHOD_NAME)) {
        assert methodCall.getParent() instanceof JExpressionStatement;

        JMethod varInitMethod;
        try {
          varInitMethod = declaredType.getMethod(FieldInitMethodRemover.VAR_INIT_METHOD_NAME,
              JPrimitiveTypeEnum.VOID.getType());
        } catch (JMethodLookupException e) {
          // All type should have a $init() method (created by ECJ)
          throw new AssertionError(e);
        }

        JAbstractMethodBody body = varInitMethod.getBody();
        assert body instanceof JMethodBody;
        JBlock varInitMethodBLock = ((JMethodBody) body).getBlock();

        if (varInitMethodBLock.getStatements().isEmpty()) {
          tr.append(new Remove(methodCall.getParent()));
        } else {
          CloneStatementVisitor csv = new CloneStatementVisitor(tr,
              methodCall.getParent(JMethod.class));
          tr.append(new Replace(methodCall.getParent(), csv.cloneStatement(varInitMethodBLock)));
        }
      }
      super.endVisit(methodCall);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (!(method instanceof JConstructor)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor v = new Visitor(tr, method.getEnclosingType());
    v.accept(method);
    tr.commit();
  }
}
