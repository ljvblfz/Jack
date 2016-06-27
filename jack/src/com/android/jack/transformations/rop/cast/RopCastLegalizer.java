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

package com.android.jack.transformations.rop.cast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.ast.ImplicitBoxingAndUnboxing;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;


/**
 * Transform body of a {@code JMethod} to contain all required cast instructions avoiding register
 * creation into Ropper.
 */
@Description("Transform body of a JMethod to contain all required cast instructions avoiding " +
    "register creation into Ropper.")
@Name("RopCastLegalizer")
@Constraint(need = JDynamicCastOperation.class, no = {ImplicitBoxingAndUnboxing.class,
    ImplicitCast.class, JCastOperation.WithIntersectionType.class})
@Transform(
    add = {RopLegalCast.class, JDynamicCastOperation.class}, remove = ThreeAddressCodeForm.class)
@Protect(add = JDynamicCastOperation.class, unprotect = @With(remove = RopLegalCast.class))
@Filter(TypeWithoutPrebuiltFilter.class)
public class RopCastLegalizer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;

    /**
     * @param request
     */
    public Visitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    /* Rop has 2 groups of cast instructions:
     * - Casts between int, long, float and double.
     * - Casts form int to byte, char and short.
     */
    @Override
    public boolean visit(@Nonnull JDynamicCastOperation cast) {

      JType castTo = cast.getType();
      if (castTo instanceof JPrimitiveType) {
        JType castedFrom = cast.getExpr().getType();

        if (castTo == JPrimitiveTypeEnum.BYTE.getType()
            || castTo == JPrimitiveTypeEnum.SHORT.getType()
            || castTo == JPrimitiveTypeEnum.CHAR.getType()) {


          if (castedFrom == JPrimitiveTypeEnum.LONG.getType()
              || castedFrom == JPrimitiveTypeEnum.FLOAT.getType()
              || castedFrom == JPrimitiveTypeEnum.DOUBLE.getType()) {
            /* The cast operation is not supported, lets split it in 2 with a intermediate INT
             */
            JExpression intermediateCastToInt = new JDynamicCastOperation(cast.getSourceInfo(),
                cast.getExpr(), JPrimitiveTypeEnum.INT.getType());
            JDynamicCastOperation replacementCast = new JDynamicCastOperation(cast.getSourceInfo(),
                intermediateCastToInt, cast.getType());
            request.append(new Replace(cast, replacementCast));
          }

        }
      }
      return super.visit(cast);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request);
    visitor.accept(method);
    request.commit();
  }

}
