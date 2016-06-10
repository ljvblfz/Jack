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

package com.android.jack.jarjar;

import com.android.jack.Options;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

public class JarjarTests {

  @Nonnull
  private RuntimeTestInfo JARJAR001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test001"),
      "com.android.jack.jarjar.test001.dx.Tests");

  @Nonnull
  private RuntimeTestInfo JARJAR003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test003"),
      "com.android.jack.jarjar.test003.dx.Tests");

  @Nonnull
  private RuntimeTestInfo JARJAR004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test004"),
      "com.android.jack.jarjar.test004.dx.Tests");

  @Nonnull
  private RuntimeTestInfo JARJAR005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test005"),
      "com.android.jack.jarjar.test005.dx.Tests");

  @Test
  @Runtime
  public void jarjar001() throws Exception {
    new RuntimeTestHelper(JARJAR001)
    .compileAndRunTest();
  }

  @Test
  @Runtime
  public void jarjar003() throws Exception {
    new RuntimeTestHelper(JARJAR003)
    .compileAndRunTest();
  }

  @Test
  public void jarjar003_1() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR003.directory, "jarjar-rules.txt")));
    File lib = AbstractTestTools.createTempFile("jarjarTest003Jack", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib,
        /* zipFiles = */ true,
        new File(JARJAR003.directory, "jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(lib)
    .srcToLib(AbstractTestTools.createTempFile("jarjarTest003dx", toolchain.getLibraryExtension()),
        /* zipFiles = */ true,
        new File(JARJAR003.directory, "dontcompile/TestWithRelocatedReference.java"));
  }

  @Test
  @Runtime
  public void jarjar004() throws Exception {

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File libToBeRenamed =
        AbstractTestTools.createTempFile("jarjarTest004Lib", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .srcToLib(libToBeRenamed,
            /* zipFiles = */ true, new File(JARJAR004.directory, "lib"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    File libReferencingLibToBeRenamed =
        AbstractTestTools.createTempFile("jarjarTest004Jack", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .addToClasspath(libToBeRenamed)
        .srcToLib(libReferencingLibToBeRenamed,
            /* zipFiles = */ true, new File(JARJAR004.directory, "jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    File renamedLib =
        AbstractTestTools.createTempFile("jarjarTest004Lib", toolchain.getLibraryExtension());
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .srcToLib(renamedLib,
            /* zipFiles = */ true, new File(JARJAR004.directory, "lib"));


    // Build dex files for runtime
    File dex1 = AbstractTestTools.createTempFile("dex1", toolchain.getExeExtension());
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(libToBeRenamed)
    .srcToExe(
        dex1,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "jack"));

    File dex2 = AbstractTestTools.createTempFile("dex2", toolchain.getExeExtension());
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(renamedLib)
    .addToClasspath(libReferencingLibToBeRenamed)
    .srcToExe(dex2,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "dontcompile/TestWithRelocatedReference.java"));

    File dex3 =  AbstractTestTools.createTempFile("dex3", toolchain.getExeExtension());
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        dex3,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "lib"));

    File dex4 =  AbstractTestTools.createTempFile("dex4", toolchain.getExeExtension());
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        dex4,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "lib"));

    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    String[] names = {"com.android.jack.jarjar.test004.dontcompile.TestWithRelocatedReference"};
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(
          0,
          runner.runJUnit(new String[] {}, AbstractTestTools.JUNIT_RUNNER_NAME, names, new File[] {
              new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
              dex1, dex2,
              dex3, dex4}));
    }

  }

  /**
   * This test checks that all types are correctly moved into another package.
   * @throws Exception
   */
  @Test
  public void jarjar005() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    File outLib =
        AbstractTestTools.createTempFile("jarjarTest005Lib", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .setJarjarRules(
            Collections.singletonList(new File(JARJAR005.directory, "jarjar-rules.txt")))
        .srcToLib(outLib,
            /* zipFiles = */ true, new File(JARJAR005.directory, "jack"));

    // Add some boiler plate code to init the configuration because, if previous compilation has
    // been done with jack api toolchain, configuration has been unset.
    Options options = new Options();
    RunnableHooks hooks = new RunnableHooks();
    options.checkValidity(hooks);
    options.getConfigBuilder(hooks).getCodecContext().setDebug();
    ThreadConfig.setConfig(options.getConfig());

    InputJackLibrary inputJackLibrary = null;
    VFS vfs = null;
    try {
      // work around a VFS bug
      // inputJackLibrary = AbstractTestTools.getInputJackLibrary(outLib);

      vfs = new ReadZipFS(
          new InputZipFile(outLib.getPath(), hooks, Existence.MUST_EXIST, ChangePermission.NOCHANGE));

      inputJackLibrary = JackLibraryFactory.getInputLibrary(vfs);

      VPath pathToA = new VPath("com/android/jack/jarjar/test005/jack/renamed/A", '/');
      VPath pathToB = new VPath("com/android/jack/jarjar/test005/jack/renamed/B", '/');
      VPath pathToC = new VPath("com/android/jack/jarjar/test005/jack/renamed/C", '/');

      inputJackLibrary.getFile(FileType.JAYCE, pathToA);
      inputJackLibrary.getFile(FileType.JAYCE, pathToB);
      inputJackLibrary.getFile(FileType.JAYCE, pathToC);
      inputJackLibrary.getFile(FileType.PREBUILT, pathToA);
      inputJackLibrary.getFile(FileType.PREBUILT, pathToB);
      inputJackLibrary.getFile(FileType.PREBUILT, pathToC);
      Iterator<InputVFile> iterator = inputJackLibrary.iterator(FileType.JAYCE);
      while (iterator.hasNext()) {
        InputVFile file = iterator.next();
        if (file.getPathFromRoot().getPathAsString('/').contains("original")) {
          Assert.fail();
        }
      }
      iterator = inputJackLibrary.iterator(FileType.PREBUILT);
      while (iterator.hasNext()) {
        InputVFile file = iterator.next();
        if (file.getPathFromRoot().getPathAsString('/').contains("original")) {
          Assert.fail();
        }
      }
    } finally {
      if (inputJackLibrary != null) {
        try {
          inputJackLibrary.close();
        } catch (UnsupportedOperationException e) {
          // work around a VFS bug
        }
      }
      if (vfs != null) {
        vfs.close();
      }
      hooks.runHooks();
      ThreadConfig.unsetConfig();
    }
  }

  /**
   * Test jarjar operation with 2 libs and a main and one jarjar operation is made with incomplete
   * classpath
   */
  @Test
  @Runtime
  public void jarjar006_1() throws Exception {
    File testRootDir = AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test006");

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    // Exclude Jill toolchain because libToLib is not supported yet.
    exclude.add(JillBasedToolchain.class);

    // Build lib1
    File lib1NoJarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib1NoJarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib1-nojarjar" + toolchain.getLibraryExtension());
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(lib1NoJarjar, /* zipFiles = */ true, new File (testRootDir,"lib1"));
    }

    // Repackage lib1
    File lib1Jarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib1Jarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib1-jarjar" + toolchain.getLibraryExtension());
      toolchain.setJarjarRules(
          Collections.singletonList(new File(testRootDir, "jarjar-rules.txt")))
      .libToLib(lib1NoJarjar, lib1Jarjar, /* zipFiles */ true);
    }

    // Build lib2 with lib1NoJarjar in the classpath
    File lib2NoJarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib2NoJarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib2-nojarjar" + toolchain.getLibraryExtension());
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(lib1NoJarjar)
      .srcToLib(lib2NoJarjar, /* zipFiles = */ true, new File (testRootDir,"lib2"));
    }

    // Repackage lib2, do not give classpath
    File lib2Jarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib2Jarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib2-jarjar"  + toolchain.getLibraryExtension());
      toolchain.setJarjarRules(
          Collections.singletonList(new File(testRootDir, "jarjar-rules.txt")))
      .libToLib(lib2NoJarjar, lib2Jarjar, /* zipFiles */ true);
    }

    // Build dex files
    File lib1Dex = AbstractTestTools.createTempFile("jarjar006", "lib1.dex.zip");
    {
      AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
      .libToExe(lib1Jarjar, lib1Dex, /* zipFile = */ true);
    }
    File lib2Dex = AbstractTestTools.createTempFile("jarjar006", "lib2.dex.zip");
    {
      AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
      .libToExe(lib2Jarjar, lib2Dex, /* zipFile = */ true);
    }
    File testsDex = AbstractTestTools.createTempFile("jarjar006", "tests.dex.zip");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(lib2Jarjar)
      .srcToExe(testsDex, /* zipFile = */ true, new File(testRootDir, "dontcompile"));
    }

    // Run to check everything went as expected
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList("com.android.jack.jarjar.test006.dontcompile.Tests"),
        RuntimeTestHelper.getJunitDex(), lib1Dex, lib2Dex, testsDex);

  }

  /**
   * Same as jarjar006_1 but jarjar operation is made with classpath
   */
  @Test
  @Runtime
  public void jarjar006_2() throws Exception {
    File testRootDir = AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test006");

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    // Exclude Jill toolchain because libToLib is not supported yet.
    exclude.add(JillBasedToolchain.class);

    // Build lib1
    File lib1NoJarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib1NoJarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib1-nojarjar" + toolchain.getLibraryExtension());
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(lib1NoJarjar, /* zipFiles = */ true, new File (testRootDir,"lib1"));
    }

    // Repackage lib1
    File lib1Jarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib1Jarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib1-jarjar" + toolchain.getLibraryExtension());
      toolchain.setJarjarRules(
          Collections.singletonList(new File(testRootDir, "jarjar-rules.txt")));
      toolchain.libToLib(lib1NoJarjar, lib1Jarjar, /* zipFiles */ true);
    }

    // Build lib2 with lib1NoJarjar in the classpath
    File lib2NoJarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib2NoJarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib2-nojarjar" + toolchain.getLibraryExtension());
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(lib1NoJarjar)
      .srcToLib(lib2NoJarjar, /* zipFiles = */ true, new File (testRootDir,"lib2"));
    }
    // Give classpath to jarjar operation
    File lib2Jarjar;
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      lib2Jarjar = AbstractTestTools.createTempFile("jarjar006",
          "lib2-jarjar" + toolchain.getLibraryExtension());
      toolchain.setJarjarRules(
          Collections.singletonList(new File(testRootDir, "jarjar-rules.txt")))
      .addToClasspath(lib1NoJarjar)
      .libToLib(lib2NoJarjar, lib2Jarjar, /* zipFiles */ true);
    }

    // Build dex files
    File lib1Dex = AbstractTestTools.createTempFile("jarjar006", "lib1.dex.zip");
    {
      AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude)
      .libToExe(lib1Jarjar, lib1Dex, /* zipFile = */ true);
    }
    File lib2Dex = AbstractTestTools.createTempFile("jarjar006", "lib2.dex.zip");
    {
      AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude)
      .libToExe(lib2Jarjar, lib2Dex, /* zipFile = */ true);
    }
    File testsDex = AbstractTestTools.createTempFile("jarjar006", "tests.dex.zip");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(lib2Jarjar)
      .srcToExe(testsDex, /* zipFile = */ true, new File(testRootDir, "dontcompile"));
    }

    // Run to check everything went as expected
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList("com.android.jack.jarjar.test006.dontcompile.Tests"),
        RuntimeTestHelper.getJunitDex(), lib1Dex, lib2Dex, testsDex);

  }

}
