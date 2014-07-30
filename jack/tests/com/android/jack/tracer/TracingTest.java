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

package com.android.jack.tracer;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.analysis.tracer.ComposedTracerBrush;
import com.android.jack.analysis.tracer.ExtendingOrImplementingClassFinder;
import com.android.jack.analysis.tracer.Tracer;
import com.android.jack.analysis.tracer.TracerBrush;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.sched.util.RunnableHooks;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class TracingTest {

  private static final int NB_TRACE = 7;

  @Test
  public void testWithShrob001() throws Exception {
    doTest(TestTools.getJackTestsWithJackFolder("shrob/test001"));
  }

  @Test
  public void testWithShrob016() throws Exception {
    doTest(TestTools.getJackTestsWithJackFolder("shrob/test016"));
  }

  @Test
  public void testWithAnnotation001() throws Exception {
    doTest(TestTools.getJackTestsWithJackFolder("annotation/test001"));
  }

  @Test
  public void testWithFlowLoop() throws Exception {
    doTest(TestTools.getJackTestsWithJackFolder("flow/loop"));
  }

  public void doTest(File fileOrSourceList) throws Exception {
    Options options =
        TestTools.buildCommandLineArgs(fileOrSourceList);
    RunnableHooks hooks = new RunnableHooks();
    try {
      JSession session = TestTools.buildSession(options, hooks);
      ExtendingOrImplementingClassFinder hierachyFinder = new ExtendingOrImplementingClassFinder();

      TracerBrush[] brushForComposed = new TracerBrush[NB_TRACE];
      Tracer[] singleTracers = new Tracer[NB_TRACE];
      for (int i = 0; i < NB_TRACE; i++) {
        brushForComposed[i] = new MultiTracerBrush(i, i, false);
        singleTracers[i] = new Tracer(new MultiTracerBrush(NB_TRACE + i, i, false));
      }

      TracerBrush brush = new ComposedTracerBrush(brushForComposed);
      Tracer tracer = new Tracer(brush);

      for (JDefinedClassOrInterface jdcoi : session.getTypesToEmit()) {
        hierachyFinder.run(jdcoi);
      }

      for (JDefinedClassOrInterface jdcoi : session.getTypesToEmit()) {
        tracer.run(jdcoi);
        for (int i = 0; i < singleTracers.length; i++) {
          singleTracers[i].run(jdcoi);
        }
      }

      for (int i = 0; i < NB_TRACE; i++) {
        ByteArrayOutputStream refOut = new ByteArrayOutputStream();
        ByteArrayOutputStream compOut = new ByteArrayOutputStream();
        MultiMarkedStructurePrinter refPrinter = new MultiMarkedStructurePrinter(
            new PrintStream(refOut), NB_TRACE + i);
        MultiMarkedStructurePrinter mdPrinter = new MultiMarkedStructurePrinter(
            new PrintStream(compOut), i);

        refPrinter.accept(session);
        mdPrinter.accept(session);

        Assert.assertEquals(refOut.toString(), compOut.toString());
      }
    } finally {
      hooks.runHooks();
    }

  }

}
