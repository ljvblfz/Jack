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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.HasModifier;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * 'class' filter.
 */
public class ClassFilter implements Expression<Collection<JClassOrInterface>, Scope> {

  @Nonnull
  private final NamePattern name;

  @Nonnull
  private Expression<Boolean, HasModifier> modifierExpression = BooleanExpression.getTrue();

  @Nonnull
  private Expression<Boolean, Scope> extendExpression = BooleanExpression.getTrue();

  @Nonnull
  private Expression<Boolean, Scope> containsExpression = BooleanExpression.getTrue();

  @Nonnull
  private Collection<Expression<Collection<?>, Scope>> annotateSets = Collections.emptyList();

  public ClassFilter(NamePattern name) {
    this.name = name;
  }

  @Override
  @Nonnull
  public Collection<JClassOrInterface> eval(@Nonnull Scope scope, @Nonnull Context context) {
    Collection<JClassOrInterface> nodes = new ArrayList<JClassOrInterface>();

    for (Iterator<? extends JClassOrInterface> iterator =
        scope.getNodesByName(JClassOrInterface.class, name).iterator(); iterator.hasNext();) {
      JClassOrInterface coi = iterator.next();
      if (coi instanceof JDefinedClassOrInterface) {
        JDefinedClassOrInterface jdcoi = (JDefinedClassOrInterface) coi;
        SubContext subContext = new SubContext(context);

        if (modifierExpression.eval(jdcoi, subContext).booleanValue()) {
          ExtendsScope extendsScope = new ExtendsScope(jdcoi);
          if (extendExpression.eval(extendsScope, subContext).booleanValue()) {
            ClassMembersScope membersScope = new ClassMembersScope(jdcoi);
            if (containsExpression.eval(membersScope, subContext).booleanValue()) {
              nodes.add(coi);
              subContext.push();
              for (Expression<Collection<?>, Scope> set : annotateSets) {
                set.eval(membersScope, context);
              }
            }
          }
        }

      } else {
        if (modifierExpression == (Object) BooleanExpression.TRUE
            && extendExpression == (Object) BooleanExpression.TRUE
            && containsExpression == (Object) BooleanExpression.TRUE) {
          nodes.add(coi);
        }
      }

    }
    return nodes;
  }

  public void setExtendsExpression(@Nonnull Expression<Boolean, Scope> expression) {
    extendExpression = expression;
  }

  public void setContainsExpression(@Nonnull Expression<Boolean, Scope> expression) {
    containsExpression = expression;
  }

  public void setAnnotateSets(@Nonnull Collection<Expression<Collection<?>, Scope>> sets) {
    annotateSets = sets;
  }

  public void setModifierExpression(@Nonnull Expression<Boolean, HasModifier> expression) {
    modifierExpression = expression;
  }

  @Nonnull
  @Override
  public String toString() {
    return "class " + name.toString();
  }

}
