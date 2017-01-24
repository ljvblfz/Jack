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

package com.android.jack.transformations.uselessif;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/**
 * This visitor removes the if statement when the condition is a boolean literal
 */
@Description("Removes useless if statement")
@Constraint(need = {JIfStatement.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class UselessIfRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final StatisticId<Counter> REMOVED_IF = new StatisticId<Counter>(
      "jack.statement.if.removed", "Removed 'if' statement",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private class UselessIfRemoverVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;

    private UselessIfRemoverVisitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      if (ifStmt.getIfExpr() instanceof JBooleanLiteral) {
        JBooleanLiteral cond = (JBooleanLiteral) ifStmt.getIfExpr();
        tracer.getStatistic(REMOVED_IF).incValue();
        if (cond.getValue()) {
          JStatement thenStmt = ifStmt.getThenStmt();
          // if (true) A else B => A
          request.append(new Replace(ifStmt, thenStmt));
        } else {
          JStatement elseStmt = ifStmt.getElseStmt();
          if (elseStmt != null) {
            // if (false) A else B => B
            request.append(new Replace(ifStmt, elseStmt));
          } else {
            // if (false) then B => []
            request.append(new Remove(ifStmt));
          }
        }
      }

      return super.visit(ifStmt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    UselessIfRemoverVisitor visitor = new UselessIfRemoverVisitor(request);
    visitor.accept(method);
    request.commit();
  }

}