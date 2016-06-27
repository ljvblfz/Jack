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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Remove non-constant initializers from {@code JNewArray}.
 */
@Description("Remove non-constant initializers from JNewArray.")
@Name("InitInNewArrayRemover")
@Constraint(need = {InitInNewArray.class, JNewArray.class})
@Transform(
    remove = {InitInNewArray.class, ThreeAddressCodeForm.class}, add = {JMultiExpression.class,
        JIntLiteral.class,
        JAsgOperation.NonReusedAsg.class,
        JLocalRef.class,
        JNewArray.class,
        JArrayRef.class})
@Use(LocalVarCreator.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class InitInNewArrayRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final LocalVarCreator lvCreator;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull LocalVarCreator lvCreator) {
      this.tr = tr;
      this.lvCreator = lvCreator;
    }

    @Override
    public boolean visit(@Nonnull JNewArray newArray) {

      List<JExpression> initializers = newArray.getInitializers();

      if (!initializers.isEmpty()
          && (!newArray.hasConstantInitializer() || initializers.size() == 1)) {
        List<JExpression> expressions = new ArrayList<JExpression>(1 + initializers.size() + 1);
        SourceInfo sourceInfo = newArray.getSourceInfo();
        JType expressionType = newArray.getType();
        JLocal array = lvCreator.createTempLocal(expressionType, sourceInfo, tr);

        List<JExpression> dims = new ArrayList<JExpression>(1);
        dims.add(new JIntLiteral(sourceInfo, initializers.size()));
        expressions.add(new JAsgOperation(sourceInfo, array.makeRef(sourceInfo),
            JNewArray.createWithDims(sourceInfo, newArray.getArrayType(), dims)));

        int index = 0;
        for (JExpression expression : initializers) {
          SourceInfo expressionInfo = expression.getSourceInfo();
          expressions.add(new JAsgOperation(expressionInfo, new JArrayRef(expressionInfo,
              array.makeRef(sourceInfo), new JIntLiteral(expressionInfo, index)), expression));
          index++;
        }

        expressions.add(array.makeRef(sourceInfo));

        tr.append(new Replace(newArray, new JMultiExpression(sourceInfo, expressions)));
      }
      return super.visit(newArray);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr, new LocalVarCreator(method, "iinar"));
    visitor.accept(method);
    tr.commit();
  }

}
