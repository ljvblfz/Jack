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

package com.android.jack.transformations.ast;

import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;


/**
 * Transform {@link JDynamicCastOperation} to have only one target type.
 */
@Description("Transform JDynamicCastOperation to have only one target type")
@Name("IntersectionTypeRemover")
@Constraint(need = JDynamicCastOperation.class)
@Transform(add = JDynamicCastOperation.class, remove = JCastOperation.WithIntersectionType.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class IntersectionTypeRemover implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JDynamicCastOperation cast) {
      JExpression expr = cast.getExpr();
      if (cast.getTypes().size() > 1) {
        for (JType type : cast.getTypes()) {
          expr = new JDynamicCastOperation(cast.getSourceInfo(), expr, type);
        }

        tr.append(new Replace(cast, expr));
      }

      return super.visit(cast);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request);
    visitor.accept(method);
    request.commit();
  }

}
