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

package com.android.jack.preprocessor;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.preprocessor.PreProcessorApplier.Entry;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.location.NoLocation;

import junit.framework.Assert;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PreProcessorTest {

  @Test
  public void test001() throws Exception {
    File testDir = TestTools.getJackTestsWithJackFolder("preprocessor/test001");
    Options args = TestTools.buildCommandLineArgs(testDir);
    RunnableHooks hooks = new RunnableHooks();
    JSession session = TestTools.buildSession(args, hooks);
    ANTLRFileStream in = new ANTLRFileStream(new File(testDir, "config.jpp").getAbsolutePath());
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);
    Collection<Rule> rules = parser.rules(session, new NoLocation());
    Scope scope = new TypeToEmitScope(session);
    Map<Entry, Rule> map = new HashMap<Entry, Rule>();
    for (Rule rule : rules) {
      Context context = new Context(rule);
      if (!rule.getSet().eval(scope, context).isEmpty()) {
        for (AddAnnotationStep request : context.getSteps()) {
          request.apply(map);
        }
      }
    }

    JAnnotationType installerAnnotationType =
        session.getPhantomLookup().getAnnotationType(
            "Lcom/android/jack/preprocessor/test001/jack/MultiDexInstaller;");
    JNodeLookup lookup = session.getLookup();
    {
      JDefinedClassOrInterface coi = lookup.getClass(
          "Lcom/android/jack/preprocessor/test001/jack/app1/ApplicationActivity1;");
      Assert.assertFalse(coi.getAnnotations(installerAnnotationType).isEmpty());
      for (JMethod method : coi.getMethods()) {
        if (method.getName().equals("noAnnotation")) {
          Assert.assertTrue(method.getAnnotations(installerAnnotationType).isEmpty());
        } else {
          Assert.assertFalse(method.getAnnotations(installerAnnotationType).isEmpty());
        }
      }
    }
    {
      JDefinedClassOrInterface coi = lookup.getClass(
          "Lcom/android/jack/preprocessor/test001/jack/app1/NoAnnotation;");
      Assert.assertTrue(coi.getAnnotations(installerAnnotationType).isEmpty());
      for (JMethod method : coi.getMethods()) {
        Assert.assertTrue(method.getAnnotations(installerAnnotationType).isEmpty());
      }
    }

    hooks.runHooks();
  }
}
