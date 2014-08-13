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
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * 'field' set.
 */
public class FieldFilter implements Expression<Collection<JField>, Scope> {

  @Nonnull
  private final NamePattern name;

  @Nonnull
  private Expression<Boolean, HasModifier> modifierExpression = BooleanExpression.getTrue();

  @Nonnull
  private final Expression<Collection<? extends JType>, Scope> type;

  public FieldFilter(@Nonnull NamePattern name,
     @Nonnull Expression<Collection<? extends JType>, Scope> type) {
    this.name = name;
    this.type = type;
  }


  @Override
  @Nonnull
  public Collection<JField> eval(@Nonnull Scope scope, @Nonnull Context context) {
    Collection<JField> nodes = new ArrayList<JField>();

    for (Iterator<? extends JField> iterator =
        scope.getNodesByName(JField.class, name).iterator(); iterator.hasNext();) {
      JField field = iterator.next();
      SubContext subContext = new SubContext(context);

        if (modifierExpression.eval(field, subContext).booleanValue()) {
          SingleTypeScope returnTypeContext = new SingleTypeScope(field.getType());
          if (!type.eval(returnTypeContext, subContext).isEmpty()) {
              nodes.add(field);
              subContext.push();
          }
        }

    }
    return nodes;
  }

  public void setModifierExpression(@Nonnull Expression<Boolean, HasModifier> expression) {
    this.modifierExpression = expression;
  }

}
