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

package com.android.jack.statistics;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.JavaSourceIr;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdapter;
import com.android.jack.scheduling.adapter.JMethodAdapter;
import com.android.jack.transformations.parent.AstChecker;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.SchedulableManager;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

@Ignore("Tree")
public class BlockStatisticsOnCore {

  private static File[] BOOTCLASSPATH;

  @BeforeClass
  public static void setUpClass() {
    BlockStatisticsOnCore.class.getClassLoader().setDefaultAssertionStatus(true);
    BOOTCLASSPATH = new File[] {TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core_intermediates/classes.jar")};
  }

  @Test
  public void computeBlockStatOnCore() throws Exception {
    Options compilerArgs = TestTools.buildCommandLineArgs(BOOTCLASSPATH, null,
        TestTools.getFromAndroidTree("libcore/luni/src/main/java/"));
    compilerArgs.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    JSession session = buildSession(compilerArgs);
    Assert.assertNotNull(session);

    BlockCountMarker bcm = session.getMarker(BlockCountMarker.class);
    Assert.assertNotNull(bcm);

    System.out.println("Existing block : " + bcm.getExistingBlockCount());
    System.out.println("Extra block : " + bcm.getExtraBlockCount());
    System.out.println("Extra block if/then : " + bcm.getExtraIfElseBlockCount());
    System.out.println("Extra block if/else : " + bcm.getExtraIfThenBlockCount());
    System.out.println("Extra block label : " + bcm.getExtraLabeledStatementBlockCount());
    System.out.println("Extra block for/body : " + bcm.getExtraForBodyBlockCount());
    System.out.println("Extra block implicit/enclosing/for : "
        + bcm.getExtraImplicitForBlockCount());
    System.out.println("Extra block while/body : " + bcm.getExtraWhileBlockCount());
  }

  @Nonnull
  private static JSession buildSession(@Nonnull Options options) throws Exception {
    JSession session = TestTools.buildJAst(options);
    Assert.assertNotNull(session);

    Scheduler scheduler = Scheduler.getScheduler();
    SchedulableManager sm = SchedulableManager.getSchedulableManager();
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(sm.getAllSchedulable());
    sr.addTargetIncludeTagOrMarker(JavaSourceIr.class);
    sr.addInitialTagOrMarker(JavaSourceIr.class);

    PlanBuilder<JSession> planBuilder = sr.getPlanBuilder(JSession.class);
    planBuilder.append(AstChecker.class);
    SubPlanBuilder<JDefinedClassOrInterface> typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
    SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
    methodPlan.append(BlockStatistics.class);

    planBuilder.getPlan().getScheduleInstance().process(session);

    return (session);
  }
}
