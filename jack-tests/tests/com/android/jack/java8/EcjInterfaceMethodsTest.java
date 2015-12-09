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

package com.android.jack.java8;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runner.AbstractRuntimeRunner;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.InterfaceMethodsTest;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class EcjInterfaceMethodsTest extends InterfaceMethodsTest {

  public EcjInterfaceMethodsTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    // No need to do setup, it decreases loaded classes.
  }

  @Override
  protected void tearDown() throws Exception {
    // No need to do tearDown, it decreases loaded classes.
  }

  public static class MyAdapter extends JUnit4TestAdapter {

    @Nonnull
    private static final List<String> negativeTests = Arrays.asList("testModifiers3",
        "testModifiers1a", "testModifiers4", "testModifiers1b", "testModifiers2", "testModifiers6",
        "testModifiers7", "testObjectMethod1", "testObjectMethod2", "testObjectMethod3",
        "testInheritedDefaultOverrides01", "testInheritedDefaultOverrides02",
        "testInheritedDefaultOverrides03", "testInheritedDefaultOverrides04", "testDefaultNonclash",
        "testDefaultNonclash2", "testDefaultNonclash3", "testDefaultNonclash4", "testAbstract02",
        "testAbstract02a", "testAbstract02b", "testAbstract02c", "testAbstract04",
        "testAbstract04a", "testAbstract05", "testAbstract06", "testAbstract06a", "testAbstract6b",
        "testAbstract6b", "testAbstract07", "testAbstract08", "testAnnotation1", "testSuperCall2",
        "testSuperCall4", "testSuperCall5", "testSuperCall6", "testStaticMethod01",
        "testStaticMethod02", "testStaticMethod04", "testStaticMethod05", "testStaticMethod07",
        "testStaticMethod08", "testStaticMethod09", "testStaticMethod10", "testStaticMethod11",
        "testStaticMethod12", "testStaticMethod13", "test406619", "testBridge01", "test400977",
        "testBug420084", "testBug421543", "testBug421543a", "test422731", "test425718",
        "test426318", "test424914", "testBug437522");

    @Nonnull
    private static final List<String> testsWithoutMain =
        Arrays.asList("testModifiers1", "testDefaultNonclash5", "testInheritedDefaultOverrides05",
            "testInheritedDefaultOverrides06", "testAbstract01", "testAbstract03",
            "testAbstract03a", "testSuperCall1", "testBug421543b", "test427478", "test427478a",
            "test423467", "test438471");

    @Nonnull
    private static final List<String> testsWithNonApplicableMsgComparison =
        Arrays.asList("test436350", "test436350a", "testBug437522a");

    @Nonnull
    private static final List<String> testsWithUnavailableApis =
        Arrays.asList("test436350a", "test453552", "test453552_comment2");

    public MyAdapter(Class<?> newTestClass) {
      super(newTestClass);
      try {
        filter(new Filter() {
          @Override
          public boolean shouldRun(Description description) {
            if (negativeTests.contains(description.getMethodName())
                || testsWithoutMain.contains(description.getMethodName())
                || testsWithNonApplicableMsgComparison.contains(description.getMethodName())
                || testsWithUnavailableApis.contains(description.getMethodName())) {
              return false;
            }
            return true;
          }

          @Override
          public String describe() {
            return "EcjInterfaceMethodsTest";
          }
        });
      } catch (NoTestsRemainException e) {
        Assert.fail();
      }
    }
  }

  public static Test suite() {
    return new MyAdapter(EcjInterfaceMethodsTest.class);
   }

  // Be careful, options of compiler are not taken into accounts
  @Override
  protected void runConformTest(String[] testFiles, String expectedOutput,
      @SuppressWarnings("rawtypes") Map customOptions) {
    runConformTest(testFiles, expectedOutput);
  }

  // Only compile source file
  @Override
  public void runConformTest(String[] srcDescription) {
    try {
      File sourceFolder = buildSourceFolder(srcDescription);

      // Build dex file
      List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
      excludeList.add(JillBasedToolchain.class);
      File dexOutDir = AbstractTestTools.createTempDir();
      JackBasedToolchain jackToolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath());
      jackToolchain.setSourceLevel(SourceLevel.JAVA_8);
      jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  //Compile and run source file
  @Override
  public void runConformTest(String[] srcDescription, String expectedResult) {
    try {
      File sourceFolder = buildSourceFolder(srcDescription);

      // Build dex file
      List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
      excludeList.add(JillBasedToolchain.class);
      File dexOutDir = AbstractTestTools.createTempDir();
      JackBasedToolchain jackToolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath());
      jackToolchain.setSourceLevel(SourceLevel.JAVA_8);
      jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);

      File dexFile = new File(dexOutDir, "classes.dex");

      List<RuntimeRunner> runnerList =
          AbstractTestTools.listRuntimeTestRunners(/* properties = */ null);
      for (RuntimeRunner runner : runnerList) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((AbstractRuntimeRunner) runner).setOutputStream(out);
        String mainClass = srcDescription[0].substring(0, srcDescription[0].lastIndexOf('.'));
        String[] trArgs = RuntimeTestHelper.getRuntimeArgs(
            runner.getClass().getSimpleName(),
            new File(
                AbstractTestTools.getTestRootDir("com.android.jack.java8"),
                "enableDefaultMethods.properties"));
        Assert.assertEquals(0, runner.run(trArgs, mainClass, dexFile));
        Assert.assertEquals(expectedResult, out.toString().trim());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Override
  protected void runConformTest(String[] testFiles, String expectedOutputString,
      String[] classLibraries, boolean shouldFlushOutputDirectory, String[] vmArguments) {
    // Be careful, parameters classLibraries, shouldFlushOutputDirectory and vmArguments are ignored
    runConformTest(testFiles, expectedOutputString);
  }

  @Nonnull
  private File buildSourceFolder(@Nonnull String[] srcDescription) throws IOException {
    assert srcDescription.length % 2 == 0;
    File sourceFolder = AbstractTestTools.createTempDir();

    for (int srcIndex = 0; srcIndex < srcDescription.length; srcIndex += 2) {
      String packageName = "";
      String fileName = srcDescription[srcIndex];
      int endOfPackage = -1;
      if ((endOfPackage = srcDescription[srcIndex].lastIndexOf('/')) != -1) {
        packageName = srcDescription[srcIndex].substring(0, endOfPackage);
        fileName = srcDescription[srcIndex].substring(endOfPackage + 1);
      }
      AbstractTestTools.createFile(sourceFolder, packageName, fileName,
          srcDescription[srcIndex + 1]);
    }

    return sourceFolder;
  }

}

