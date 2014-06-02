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


import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdapter;
import com.android.jack.scheduling.adapter.JMethodAdapter;
import com.android.jack.transformations.parent.ParentSetterChecker;
import com.android.jack.util.filter.SignatureMethodFilter;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

public class SynchronizedTest {

  @Nonnull
  private static final String CLASS_BINARY_NAME =
    "com/android/jack/synchronize/test002/jack/Synchronized2";
  @Nonnull
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File FILE = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @BeforeClass
  public static void setUp() throws Exception {
    SynchronizedTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void synchronizedStaticMethod() throws Exception {
    JMethod method = buildMethodAndRunSynchronizeTransformer("syncStaticMethod(I)I");

    Assert.assertTrue(method.isSynchronized());

    // 0: lock, 1:try or 0: assign, 1: lock, 2: try
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    List<JStatement> statements = body.getBlock().getStatements();
    int pos = 0;
    JStatement jlock = statements.get(pos++);
    if (!(jlock instanceof JLock)) {
      jlock = statements.get(pos++);
    }

    Assert.assertEquals(true, jlock instanceof JLock);

    JStatement tryStmt = statements.get(pos);
    Assert.assertEquals(true, tryStmt instanceof JTryStatement);
    JTryStatement tryBlock = (JTryStatement) tryStmt;

    JBlock finallyBlock = tryBlock.getFinallyBlock();
    assert finallyBlock != null;
    Assert.assertEquals(true, finallyBlock.getStatements().get(0) instanceof JUnlock);
  }

  @Test
  public void synchronizedInstanceMethod() throws Exception {
    JMethod method = buildMethodAndRunSynchronizeTransformer("syncInstanceMethod(I)I");

    Assert.assertTrue(method.isSynchronized());

    // 0: lock, 1:try or 0: assign, 1: lock, 2: try
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    List<JStatement> statements = body.getBlock().getStatements();
    int pos = 0;
    JStatement jlock = statements.get(pos++);

    Assert.assertEquals(true, jlock instanceof JLock);

    JStatement trytStmt = statements.get(pos);
    Assert.assertEquals(true, trytStmt instanceof JTryStatement);
    JTryStatement tryBlock = (JTryStatement) trytStmt;

    JBlock finallyBlock = tryBlock.getFinallyBlock();
    assert finallyBlock != null;
    Assert.assertEquals(true, finallyBlock.getStatements().get(0) instanceof JUnlock);
  }

  @Test
  public void synchronizedBlock() throws Exception {
    JMethod method = buildMethodAndRunSynchronizeTransformer("syncBlock(I)I");

    Assert.assertTrue(!method.isSynchronized());

    // 0:JAsg, 1: JLock, 2:JTry
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    List<JStatement> statements = body.getBlock().getStatements();
    JStatement jlock = statements.get(1);
    Assert.assertEquals(true, jlock instanceof JLock);
    Assert.assertEquals(true, ((JLock)jlock).getLockExpr() instanceof JVariableRef);

    JStatement firstStmt = statements.get(2);
    Assert.assertEquals(true, firstStmt instanceof JTryStatement);
    JTryStatement tryBlock = (JTryStatement) firstStmt;

    JBlock finallyBlock = tryBlock.getFinallyBlock();
    assert finallyBlock != null;
    Assert.assertEquals(true, finallyBlock.getStatements().get(0) instanceof JUnlock);
  }

  private static JMethod buildMethodAndRunSynchronizeTransformer(String methodSignature)
      throws Exception {
    Options options = TestTools.buildCommandLineArgs(FILE);
    options.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    options.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);
    JSession session = TestTools.buildJAst(options);
    Assert.assertNotNull(session);

    Scheduler scheduler = Scheduler.getScheduler();
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scheduler.getAllSchedulable());
    sr.addInitialTagsOrMarkers(Jack.getJavaSourceInitialTagSet());

    PlanBuilder<JSession> planBuilder = sr.getPlanBuilder(JSession.class);
    planBuilder.append(ParentSetterChecker.class);
    SubPlanBuilder<JDefinedClassOrInterface> typePlan =
        planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
    SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
    methodPlan.append(ImplicitBlocks.class);
    methodPlan.append(SynchronizeTransformer.class);

    planBuilder.getPlan().getScheduleInstance().process(session);

    JDefinedClassOrInterface type = (JDefinedClassOrInterface)
        session.getLookup().getType(CLASS_SIGNATURE);
    Assert.assertNotNull(type);

    JMethod foundMethod = TestTools.getMethod(type, methodSignature);

    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }
}
