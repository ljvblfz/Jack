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

import com.android.jack.test.eclipse.jdt.core.tests.compiler.regression.LambdaExpressionsTest;
import com.android.jack.test.runner.AbstractRuntimeRunner;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.internal.AssumptionViolatedException;
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

public class EcjLambdaTest extends LambdaExpressionsTest {

  public EcjLambdaTest(@Nonnull String name) {
    super(name);
  }

  public static class MyAdapter extends JUnit4TestAdapter {

    public MyAdapter(Class<?> newTestClass) {
      super(newTestClass);
      try {
        filter(new Filter() {
          @Override
          public boolean shouldRun(Description description) {
            if (testWithApiUsage.contains(description.getMethodName())
                || testWithOtherErrorMsg.contains(description.getMethodName())
                || EcjLambdaTestPostM.testForNewRuntime.contains(description.getMethodName())
                || testBugECJ.contains(description.getMethodName())) {
              return false;
            }
            return true;
          }

          @Override
          public String describe() {
            return "EcjLambdaTest";
          }
        });
      } catch (NoTestsRemainException e) {
        Assert.fail();
      }
    }
  }

  @Nonnull
  private static final List<String> testBugJavac = Arrays.asList("test430035d",
      "test430035e", "test428261a", "test044", "test051", "testReferenceExpressionInference3a");

  @Nonnull
  private static final List<String> testBugECJ = Arrays.asList("test055");

  @Nonnull
  private static final List<String> testWithOtherErrorMsg =
      Arrays.asList("test015", "test045", "test056", "test425512", "test430766", "test430766a",
          "testBug424742", "test424589", "testReferenceExpressionInference3b"
  );

  @Nonnull
  private static final List<String> testWithApiUsage = Arrays.asList("test003", "test004",
      "test017", "test424226", "test427483", "test427962", "test428112", "test423684", "test428642",
      "test428857", "test428857a", "test428857b", "test428857c", "test429112", "test429112a",
      "test429112b", "test429112c", "test429759", "test429763", "test429763a", "test429948",
      "test430015", "test429969", "test430310", "test430310a", "test430310b", "test430310c",
      "test430043", "test430035", "test430035a", "test441929", "test431190", "test434297",
      "test432625", "test432682", "test436542", "test437781", "test438534", "test432110",
      "test432531", "test432605", "test432619", "test432619a", "test440152", "test440152a",
      "test441907", "test443889", "test444772", "test444773", "test444785", "test445949",
      "test445949a", "test447119", "test447119a", "test447119b", "test447119c", "test447119d",
      "test447119e", "test448802", "test449063", "test449063a", "test449063b", "test449063c",
      "test449063d", "test449063e", "test456395", "test459305", "test467825", "test467825a",
      "testBug419048_1", "testBug419048_2", "testBug419048_3", "testreduced432605", "test461004");

  @Override
  protected void setUp() throws Exception {
    // No need to do setup, it decreases loaded classes.
  }

  @Override
  protected void tearDown() throws Exception {
    // No need to do tearDown, it decreases loaded classes.
  }

  public static Test suite() {
   return new MyAdapter(EcjLambdaTest.class);
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
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    // These tests must be exclude from the Jill tool-chain because they do not compile with it
    if (getName().equals("testReferenceExpressionInference3a")) {
      excludeList.add(JillBasedToolchain.class);
      excludeList.add(LegacyBasedToolchain.class);
    }
    excludeList.add(JackApiV01.class);

    try {
      File sourceFolder = buildSourceFolder(srcDescription);
      File dexOutDir = AbstractTestTools.createTempDir();

      // Build dex file
      AndroidToolchain jackToolchain = createToolchain(excludeList);
      jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);
    } catch (AssumptionViolatedException e) {
      // Handle JUnit4 feature in JUnit3 tests.
      return;
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  //Compile and run source file
  @Override
  public void runConformTest(String[] srcDescription, String expectedResult) {
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    // These tests must be exclude from the Jill tool-chain because they do not compile with it
    if (getName().equals("test430035d") ||
        getName().equals("test430035e") ||
        getName().equals("test428261a") ||
        getName().equals("test044")) {
      excludeList.add(JillBasedToolchain.class);
    }
    // This tests must be exclude from Jill tool-chain because it raised an illegal access error at
    // runtime, but is it not the case with Jack
    if (getName().equals("test051")) {
      excludeList.add(JillBasedToolchain.class);
    }
    if (testBugJavac.contains(getName())) {
      // This tests does not compile with Javac exclude them
      excludeList.add(LegacyBasedToolchain.class);
    }
    excludeList.add(JackApiV01.class);

    try {
      File dexOutDir = AbstractTestTools.createTempDir();
      File sourceFolder = buildSourceFolder(srcDescription);

      // Build dex file
      AndroidToolchain jackToolchain = createToolchain(excludeList);
      jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);

      File dexFile = new File(dexOutDir, "classes.dex");

      List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners();
      for (RuntimeRunner runner : runnerList) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((AbstractRuntimeRunner) runner).setOutputStream(out);
        String mainClass = srcDescription[0].substring(0, srcDescription[0].lastIndexOf('.'));
        Assert.assertEquals(0, runner.run(new String[0], mainClass, dexFile));
        Assert.assertEquals(expectedResult, out.toString().trim());
      }
    } catch (AssumptionViolatedException e) {
      // Handle JUnit4 feature in JUnit3 tests.
      return;
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
  private File buildSourceFolder(@Nonnull String[] srcDescription)
      throws IOException, CannotCreateFileException, CannotChangePermissionException {
    assert srcDescription.length % 2 == 0;
    File sourceFolder = AbstractTestTools.createTempDir();

    for (int srcIndex = 0; srcIndex < srcDescription.length; srcIndex += 2) {
      String packageName = "";
      String fileName = srcDescription[srcIndex];
      int endOfPackage = -1;
      if ((endOfPackage = srcDescription[srcIndex].indexOf('/')) != -1) {
        packageName = srcDescription[srcIndex].substring(0, endOfPackage);
        fileName = srcDescription[srcIndex].substring(endOfPackage + 1);
      }
      AbstractTestTools.createFile(sourceFolder, packageName, fileName,
          srcDescription[srcIndex + 1]);
    }

    return sourceFolder;
  }

  protected AndroidToolchain createToolchain(
      @Nonnull List<Class<? extends IToolchain>> excludeList) throws Exception {

    AndroidToolchain jackToolchain = null;
    jackToolchain =
          AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, excludeList);

    File[] bootclasspath = jackToolchain.getDefaultBootClasspath();
    jackToolchain.addToClasspath(bootclasspath);
    jackToolchain.setSourceLevel(SourceLevel.JAVA_8);

    return jackToolchain;

  }

}
