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

package com.android.jack.transformations.ast.switches;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.ast.UnassignedValues;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * Removes {@link JSwitchStatement} with only default case.
 */
@Description("Removes switches with only default case.")
@Name("UselessSwitchesRemover")
@Constraint(need = {JSwitchStatement.class, NoImplicitBlock.class})
@Transform(add = UnassignedValues.class, remove = {ThreeAddressCodeForm.class,
    UselessSwitches.class})
@Protect(add = {JSwitchStatement.class, JExpressionStatement.class, JExpressionStatement.class},
    unprotect = @With(add = UselessSwitches.class))
public class UselessSwitchesRemover implements RunnableSchedulable<JMethod> {

  public static final StatisticId<Percent> SWITCH_WITH_CST = new StatisticId<Percent>(
      "jack.statement.switch.constant", "Switch that have a constant expression",
      PercentImpl.class, Percent.class);

  public static final StatisticId<Percent> SWITCH_WITH_ONLY_DEFAULT = new StatisticId<Percent>(
      "jack.statement.switch.default", "Switch that have only a default case",
      PercentImpl.class, Percent.class);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final Stack<Boolean> removeBreakOrCase = new Stack<Boolean>();

    @Nonnull
    private final Tracer tracer;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
      tracer = TracerFactory.getTracer();
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement jswitch) {
      removeBreakOrCase.push(jswitch.getCases().isEmpty() ? Boolean.valueOf(true) :
        Boolean.valueOf(false));
      return super.visit(jswitch);
    }

    @Override
    public boolean visit(@Nonnull JDoStatement doStmt) {
      removeBreakOrCase.push(Boolean.valueOf(false));
      return super.visit(doStmt);
    }

    @Override
    public void endVisit(@Nonnull JDoStatement x) {
      removeBreakOrCase.pop();
      super.endVisit(x);
    }

    @Override
    public boolean visit(@Nonnull JForStatement x) {
      removeBreakOrCase.push(Boolean.valueOf(false));
      return super.visit(x);
    }

    @Override
    public void endVisit(@Nonnull JForStatement x) {
      removeBreakOrCase.pop();
      super.endVisit(x);
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement x) {
      removeBreakOrCase.push(Boolean.valueOf(false));
      return super.visit(x);
    }

    @Override
    public void endVisit(@Nonnull JWhileStatement x) {
      removeBreakOrCase.pop();
      super.endVisit(x);
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement x) {
      removeBreakOrCase.push(Boolean.valueOf(false));
      return super.visit(x);
    }

    @Override
    public void endVisit(@Nonnull JLabeledStatement x) {
      removeBreakOrCase.pop();
      super.endVisit(x);
    }

    @Override
    public void endVisit(@Nonnull JCaseStatement caseStmt) {
      boolean remove = removeBreakOrCase.peek().booleanValue();

      if (remove && caseStmt.getExpr() == null) {
        // Remove default
        tr.append(new Remove(caseStmt));
      } else {
        assert !remove;
      }

      super.endVisit(caseStmt);
    }

    @Override
    public void endVisit(@Nonnull JBreakStatement breakStmt) {
      if (removeBreakOrCase.peek().booleanValue()) {
        tr.append(new Remove(breakStmt));
      }
      super.endVisit(breakStmt);
    }

    @Override
    public void endVisit(@Nonnull JSwitchStatement jswitch) {
      tracer.getStatistic(SWITCH_WITH_CST).add(jswitch.getExpr() instanceof JValueLiteral);

      if (removeBreakOrCase.pop().booleanValue()) {
        tr.append(new AppendBefore(jswitch, jswitch.getExpr().makeStatement()));

        JBlock switchBody = jswitch.getBody();
        List<JStatement> stmts = switchBody.getStatements();
        if (stmts.size() >= 1) {
          tr.append(new AppendBefore(jswitch, switchBody));
        }

        tr.append(new Remove(jswitch));
        tracer.getStatistic(SWITCH_WITH_ONLY_DEFAULT).addTrue();
      } else {
        tracer.getStatistic(SWITCH_WITH_ONLY_DEFAULT).addFalse();
      }
      super.endVisit(jswitch);
    }

  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }
}
