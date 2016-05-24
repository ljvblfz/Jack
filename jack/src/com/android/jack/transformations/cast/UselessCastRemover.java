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

package com.android.jack.transformations.cast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;
/**
 * Removes useless casts.
 */
@Description("Removes useless casts.")
@Name("UselessCastRemover")
@Constraint(need = JDynamicCastOperation.class, no = JCastOperation.WithIntersectionType.class)
@Transform(remove = SourceCast.class)
@Filter(TypeWithoutPrebuiltFilter.class)
// Uses canBeSafelyUpcast which browse hierarchy.
@Access(JSession.class)
public class UselessCastRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;


    public Visitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public void endVisit(@Nonnull JCastOperation cast) {
      JType destType = cast.getType();
      JExpression castedExpr = cast.getExpr();
      JType srcType = castedExpr.getType();
      // Do not remove cast of 'null' expression otherwise type is lost
      if (!(castedExpr instanceof JNullLiteral)) {
        if (srcType instanceof JReferenceType && destType instanceof JReferenceType) {
          if (((JReferenceType) srcType).canBeSafelyUpcast((JReferenceType) destType)) {
            request.append(new Replace(cast, castedExpr));
          }
        }
      }
      super.endVisit(cast);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract()
        || !filter.accept(UselessCastRemover.class, method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request);
    visitor.accept(method);
    request.commit();
  }
}
