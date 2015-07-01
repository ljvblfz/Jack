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
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;
import com.android.sched.util.RunnableHooks;

import junit.framework.Assert;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PreProcessorTests {

  @BeforeClass
  public static void setUpClass() throws Exception {

  }

  @Test
  public void test001_002() throws Exception {
    File testDir = AbstractTestTools.getTestRootDir("com.android.jack.preprocessor.test001.jack");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(PreProcessor.ENABLE.getName(), "true");
    toolchain.addProperty(PreProcessor.FILE.getName(),
        new File(testDir, "config.jpp").getAbsolutePath());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(AbstractTestTools.createTempFile("annotationAdderTest", ".out.zip"),
        /* zipFile = */ true,
        testDir);
  }

  @Test
  public void test001_001() throws Exception {
    File testDir = AbstractTestTools.getTestRootDir("com.android.jack.preprocessor.test001.jack");
    File tempDir = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(tempDir,
        /* zipFiles = */ false,
        testDir);

    Options args = TestTools.buildCommandLineArgs(
        new File(testDir, "app1/ApplicationActivity1.java"));
    args.setClasspath(AbstractTestTools.getClasspathsAsString(toolchain.getDefaultBootClasspath(),
        new File[] {tempDir}));
    RunnableHooks hooks = new RunnableHooks();
    JSession session = TestTools.buildSession(args, hooks);
    ANTLRFileStream in = new ANTLRFileStream(new File(testDir, "config.jpp").getAbsolutePath());
    PreProcessorLexer lexer = new PreProcessorLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    PreProcessorParser parser = new PreProcessorParser(tokens);
    Collection<Rule> rules = parser.rules(session);
    Scope scope = new TypeToEmitScope(session);
    for (Rule rule : rules) {
      Context context = new Context();
      if (!rule.getSet().eval(scope, context).isEmpty()) {
        context.getRequest(session).commit();
      }
    }

    JAnnotationType installerAnnotation = session.getPhantomLookup().getAnnotationType(
        "Lcom/android/jack/preprocessor/test001/jack/MultiDexInstaller;");
    JNodeLookup lookup = session.getLookup();
    {
      JDefinedClassOrInterface coi = lookup.getClass(
          "Lcom/android/jack/preprocessor/test001/jack/app1/ApplicationActivity1;");
      Assert.assertFalse(coi.getAnnotations(installerAnnotation).isEmpty());
      for (JMethod method : coi.getMethods()) {
        if (method.getName().equals("noAnnotation")) {
          Assert.assertTrue(method.getAnnotations(installerAnnotation).isEmpty());
        } else {
          Assert.assertFalse(method.getAnnotations(installerAnnotation).isEmpty());
        }
      }
    }

    hooks.runHooks();
  }
}
