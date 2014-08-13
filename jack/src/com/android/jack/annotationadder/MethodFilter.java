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

package com.android.jack.annotationadder;

import com.android.jack.ir.ast.HasModifier;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * 'method' set.
 */
public class MethodFilter implements Expression<Collection<JMethod>, Scope> {

  @Nonnull
  private final NamePattern name;

  @Nonnull
  private Expression<Boolean, HasModifier> modifierExpression = BooleanExpression.getTrue();

  @Nonnull
  private final Expression<Collection<? extends JType>, Scope> returnType;

  @Nonnull
  private List<Expression<Collection<? extends JType>, Scope>> argsType = Collections.emptyList();

  public MethodFilter(
      @Nonnull NamePattern name,
      @Nonnull Expression<Collection<? extends JType>, Scope> returnType) {
    this.name = name;
    this.returnType = returnType;
  }


  @Override
  @Nonnull
  public Collection<JMethod> eval(@Nonnull Scope scope, @Nonnull Context context) {
    Collection<JMethod> nodes = new ArrayList<JMethod>();

    for (Iterator<? extends JMethod> iterator =
        scope.getNodesByName(JMethod.class, name).iterator(); iterator.hasNext();) {
      JMethod method = iterator.next();
      List<JParameter> params = method.getParams();
      if (params.size() == argsType.size()) {
        SubContext subContext = new SubContext(context);
        if (modifierExpression.eval(method, subContext).booleanValue()) {
          SingleTypeScope returnTypeContext = new SingleTypeScope(method.getType());
          if (!returnType.eval(returnTypeContext, subContext).isEmpty()) {
            boolean argsTypeMatched = true;
            Iterator<Expression<Collection<? extends JType>, Scope>> matchIter =
                argsType.iterator();
            for (Iterator<JParameter> paramIter = params.iterator();
                argsTypeMatched && paramIter.hasNext();) {
              argsTypeMatched =
                  !matchIter.next().eval(
                      new SingleTypeScope(paramIter.next().getType()), subContext).isEmpty();
            }
            if (argsTypeMatched) {
              nodes.add(method);
              subContext.push();
            }
          }
        }

      }
    }
    return nodes;
  }

  public void addArg(@Nonnull Expression<Collection<? extends JType>, Scope> arg) {
    if (argsType.isEmpty()) {
      argsType = new ArrayList<Expression<Collection<? extends JType>, Scope>>();
    }
    argsType.add(arg);
  }

  public void setModifierExpression(@Nonnull Expression<Boolean, HasModifier> expression) {
    this.modifierExpression = expression;
  }
}
