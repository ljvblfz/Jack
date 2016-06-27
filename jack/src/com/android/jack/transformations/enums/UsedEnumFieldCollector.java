/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations.enums;

import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Collect all enum fields used into a switch.
 */
@Description("Collect all enum fields used into a switch")
@Constraint(need = {JSwitchStatement.SwitchWithEnum.class})
@Transform(add = {SwitchEnumSupport.UsedEnumField.class})
@Filter(SourceTypeFilter.class)
public class UsedEnumFieldCollector implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Collector extends JVisitor {
    @Nonnull
    private final Set<JFieldId> usedEnumField = new HashSet<JFieldId>();

    @Override
    public void endVisit(@Nonnull JDefinedClassOrInterface clOrI) {
      clOrI.addMarker(new SwitchEnumSupport.UsedEnumField(usedEnumField));
      super.endVisit(clOrI);
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JLiteral caseExpr = caseStmt.getExpr();

      if (caseExpr != null && caseExpr instanceof JEnumLiteral) {
        usedEnumField.add(((JEnumLiteral) caseExpr).getFieldId());
      }

      return super.visit(caseStmt);
    }
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) {
    Collector c = new Collector();
    c.accept(declaredType);
  }
}
