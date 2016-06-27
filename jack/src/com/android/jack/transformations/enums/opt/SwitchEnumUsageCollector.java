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

package com.android.jack.transformations.enums.opt;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/**
 * This class is used to collect the information of how many classes in which an enum is used
 * in switch statements. This information will finally be used in {@link SyntheticClassManager}
 * to decide if it is worth creating new synthetic switch map class because of class overhead.
 * For example, if an enum is only used in switch statements of one class, then there is no need
 * to create another synthetic class. In that case, follow {@link SwitchEnumSupport}'s solution.
 */
@Description("Collect the number of classes using each enum in switch statements.")
@Name("SwitchEnumUsageCollector")
@Constraint(need = {JSwitchStatement.class, JDefinedClass.class, JDefinedEnum.class})
@Filter(SourceTypeFilter.class)
@Transform(add = {SwitchEnumUsageMarker.class, EnumFieldMarker.class})
// Access to a JPackage.
@Access(JSession.class)
public class SwitchEnumUsageCollector implements RunnableSchedulable<JMethod> {
  // the statistic counting the number of synthetic switch map initializer eliminated during
  // current compilation.
  @Nonnull
  public static final StatisticId<Counter> SYNTHETIC_SWITCHMAP_METHOD = new StatisticId<Counter>(
      "jack.optimization.enum.switch.synthetic.method.decrease",
      "Total number of synthetic method eliminated", CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer statisticTracer = TracerFactory.getTracer();

  public SwitchEnumUsageCollector() {}

  @Override
  public void run(@Nonnull JMethod method) {
    JDefinedClassOrInterface definedClass = method.getEnclosingType();
    // check if both the method and enclosing class are concrete
    if (!(definedClass instanceof JDefinedClass) || method.isNative() || method.isAbstract()) {
      return;
    }
    Visitor visitor = new Visitor((JDefinedClass) definedClass);
    visitor.accept(method);
  }

  private class Visitor extends JVisitor {
    @Nonnull
    private final JDefinedClass enclosingClass;

    public Visitor(@Nonnull JDefinedClass enclosingClass) {
      this.enclosingClass = enclosingClass;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      JExpression switchExpr = switchStmt.getExpr();
      JType switchExprType = switchExpr.getType();

      if (switchExprType instanceof JDefinedEnum) {
        JDefinedEnum enumType = (JDefinedEnum) switchExprType;
        JPackage enclosingPackage;
        if (enumType.isPublic()) {
          enclosingPackage = Jack.getSession().getLookup().getOrCreatePackage(
              SyntheticClassManager.PublicSyntheticSwitchmapClassPkgName);
        } else {
          enclosingPackage = enumType.getEnclosingPackage();
        }
        // the enum usage marker is used to tell the usage of enum under a package
        SwitchEnumUsageMarker usageMarker = enclosingPackage.getMarker(SwitchEnumUsageMarker.class);
        if (usageMarker == null) {
          SwitchEnumUsageMarker newMarker = new SwitchEnumUsageMarker(enclosingPackage);
          usageMarker = enclosingPackage.addMarkerIfAbsent(newMarker);
          if (usageMarker == null) {
            usageMarker = newMarker;
          }
        }
        // add the enclosing class into user set. This information will be used during
        // optimization stage
        if (usageMarker.addEnumUsage(enclosingClass, enumType) && usageMarker.getUses() > 1) {
          // the number of eliminated synthetic switch map initializer is total number of
          // uses - 1
          statisticTracer.getStatistic(SYNTHETIC_SWITCHMAP_METHOD).incValue();
        }
      }
      return super.visit(switchStmt);
    }
  }
}