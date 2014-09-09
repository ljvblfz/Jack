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
import com.android.jack.TestTools;
import com.android.jack.ir.JavaSourceIr;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdapter;
import com.android.jack.scheduling.adapter.JMethodAdapter;
import com.android.jack.transformations.parent.ParentChecker;
import com.android.jack.util.filter.SignatureMethodFilter;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class ImplicitBlockTest {
  @Nonnull
  private static final String CLASS_BINARY_NAME = "com/android/jack/block/test001/jack/ImplicitBlockSample";
  @Nonnull
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File FILE = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @BeforeClass
  public static void setUp() throws Exception {
    ImplicitBlockTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void implicitBlockIfThen() throws Exception {
    buildMethodWithImplicitBlock("ifThen(I)I");
  }

  @Test
  public void implicitBlockIfElse() throws Exception {
    buildMethodWithImplicitBlock("ifElse(I)I");
  }

  @Test
  public void implicitBlockLabelStmt001() throws Exception {
    buildMethodWithImplicitBlock("labelStmt001(I)I");
  }

  @Test
  public void implicitBlockLabelStmt002() throws Exception {
    buildMethodWithImplicitBlock("labelStmt002(I)I");
  }

  @Test
  public void implicitBlockLabelStmt003() throws Exception {
    buildMethodWithImplicitBlock("labelStmt003(I)I");
  }

  @Test
  public void implicitBlockForBody001() throws Exception {
    buildMethodWithImplicitBlock("forBody001(I)I");
  }

  @Test
  public void implicitBlockForBody002() throws Exception {
    buildMethodWithImplicitBlock("forBody002(I)I");
  }

  @Test
  public void implicitBlockWhileBody() throws Exception {
    buildMethodWithImplicitBlock("whileBody(I)I");
  }

  @Test
  public void implicitBlockCase001() throws Exception {
    buildMethodWithImplicitBlock("caseStmt001(I)I");
  }

  @Test
  public void implicitBlockCase002() throws Exception {
    buildMethodWithImplicitBlock("caseStmt002(I)I");
  }

  private static JMethod buildMethodWithImplicitBlock(String methodSignature) throws Exception {
    Options options = TestTools.buildCommandLineArgs(FILE);
    options.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    options.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);
    JSession session = TestTools.buildJAst(options);
    Assert.assertNotNull(session);

    Scheduler scheduler = Scheduler.getScheduler();
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scheduler.getAllSchedulable());
    sr.addTargetIncludeTagOrMarker(JavaSourceIr.class);
    sr.addInitialTagOrMarker(JavaSourceIr.class);

    PlanBuilder<JSession> planBuilder = sr.getPlanBuilder(JSession.class);
    planBuilder.append(ParentChecker.class);
    SubPlanBuilder<JDefinedClassOrInterface> typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
    SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
    methodPlan.append(ImplicitBlocks.class);
    methodPlan.append(ImplicitBlocksChecker.class);

    planBuilder.getPlan().getScheduleInstance().process(session);

    JDefinedClassOrInterface type = (JDefinedClassOrInterface)
        session.getLookup().getType(CLASS_SIGNATURE);
    Assert.assertNotNull(type);

    JMethod foundMethod = TestTools.getMethod(type, methodSignature);

    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }
}
