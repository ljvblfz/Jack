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

package com.android.jack.cfg;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.JavaSourceIr;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdaptor;
import com.android.jack.scheduling.adapter.JMethodAdaptor;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.ast.ImplicitBlocks;
import com.android.jack.transformations.ast.ImplicitBlocksChecker;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.jack.transformations.exceptions.TryCatchRemover;
import com.android.jack.transformations.finallyblock.FinallyRemover;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;

import junit.framework.Assert;

public class CfgTools {

  public static JMethod buildCfg(
      String classSignature, String methodSignature, Options options) throws Exception {
    JProgram jprogram = TestTools.buildJAst(options);
    Assert.assertNotNull(jprogram);


    Scheduler scheduler = Scheduler.getScheduler();
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scheduler.getAllSchedulable());
    sr.addTargetIncludeTagOrMarker(ControlFlowGraph.class);

    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();
    set.add(JavaSourceIr.class);
    set.add(ThreeAddressCodeForm.class);
    set.add(JExceptionRuntimeValue.class);
    set.add(OriginalNames.class);
    set.remove(JLoop.class);
    set.remove(UselessSwitches.class);
    set.remove(JBreakStatement.class);
    set.remove(JContinueStatement.class);
    set.remove(JSynchronizedBlock.class);
    set.remove(JTryStatement.FinallyBlock.class);
    set.remove(JFieldInitializer.class);
    sr.addInitialTagsOrMarkers(set);

    PlanBuilder<JProgram> progPlan = sr.getPlanBuilder(JProgram.class);
    SubPlanBuilder<JDefinedClassOrInterface> typePlan = progPlan.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
    SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
    methodPlan.append(ImplicitBlocks.class);
    methodPlan.append(ImplicitBlocksChecker.class);
    methodPlan.append(FinallyRemover.class);
    methodPlan.append(TryCatchRemover.class);
    methodPlan.append(CfgBuilder.class);

    progPlan.getPlan().getScheduleInstance().process(jprogram);

    JDefinedClassOrInterface type = (JDefinedClassOrInterface)
        jprogram.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod foundMethod = TestTools.getMethod(type, methodSignature);
    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }
}
