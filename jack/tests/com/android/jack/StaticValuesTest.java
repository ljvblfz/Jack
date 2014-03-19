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

package com.android.jack;

import com.android.jack.backend.dex.FieldInitializerRemover;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdaptor;
import com.android.jack.scheduling.adapter.JFieldAdaptor;
import com.android.jack.util.filter.SupportedMethods;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for compilation of static field access.
 */
public class StaticValuesTest {

  private static final String CLINIT = "<clinit>";

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testGeneratedClinit1() throws Exception {
    Assert.assertTrue(
        isEmptyMethod(compileAndGetClinit("com/android/jack/field/static004/jack/Data1")));
  }

  @Test
  public void testGeneratedClinit2() throws Exception {
    Assert.assertTrue(
        isEmptyMethod(compileAndGetClinit("com/android/jack/field/static004/jack/Data2")));
  }

  @Test
  public void testGeneratedClinit3() throws Exception {
    Assert.assertFalse(
        isEmptyMethod(compileAndGetClinit("com/android/jack/field/static004/jack/Data3")));
  }

  @Test
  public void testGeneratedClinit4() throws Exception {
    Assert.assertTrue(
        isEmptyMethod(compileAndGetClinit("com/android/jack/field/static004/jack/Data4")));
  }

  @Test
  public void testGeneratedClinit5() throws Exception {
    Assert.assertFalse(
        isEmptyMethod(compileAndGetClinit("com/android/jack/field/static004/jack/Data5")));
  }

  @Test
  public void testGeneratedClinit6() throws Exception {
    final String classBinaryName = "com/android/jack/field/static004/jack/Data6";
    Options options = TestTools
        .buildCommandLineArgs(TestTools.getJackTestFromBinaryName(classBinaryName));
    options.dxLegacy = false;
    Assert.assertTrue(
        isEmptyMethod(compileAndGetClinit(classBinaryName, options)));

    options = TestTools
        .buildCommandLineArgs(TestTools.getJackTestFromBinaryName(classBinaryName));
    options.dxLegacy = true;
    Assert.assertFalse(
        isEmptyMethod(compileAndGetClinit(classBinaryName, options)));
  }

  private static JMethod compileAndGetClinit(String classBinaryName) throws Exception {
    Options options = TestTools
        .buildCommandLineArgs(TestTools.getJackTestFromBinaryName(classBinaryName));
    return compileAndGetClinit(classBinaryName, options);
  }

  public static JMethod compileAndGetClinit(String classBinaryName, Options options)
      throws Exception {
    options.setFilter(new SupportedMethods());

    JProgram jprogram = TestTools.buildJAst(options);
    Assert.assertNotNull(jprogram);


    Scheduler scheduler = Scheduler.getScheduler();
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scheduler.getAllSchedulable());

    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();
    set.add(JFieldInitializer.class);
    sr.addInitialTagsOrMarkers(set);

    PlanBuilder<JProgram> progPlan = sr.getPlanBuilder(JProgram.class);
    SubPlanBuilder<JDefinedClassOrInterface> typePlan =
        progPlan.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
    SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdaptor.class);
    fieldPlan.append(FieldInitializerRemover.class);

    progPlan.getPlan().getScheduleInstance().process(jprogram);

    JDefinedClassOrInterface declaredType =
        (JDefinedClassOrInterface) jprogram.getLookup().getType("L" + classBinaryName + ";");
    return declaredType.getMethod(CLINIT, JPrimitiveTypeEnum.VOID.getType());
  }

  private static boolean isEmptyMethod(JMethod method) {
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    return body.getBlock().getStatements().isEmpty();
  }
}
