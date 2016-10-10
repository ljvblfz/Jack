/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.java7;

import com.android.jack.test.eclipse.jdt.core.tests.compiler.regression.PolymorphicSignatureTest;
import com.android.jack.test.runner.AbstractRuntimeRunner;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class EcjPolymorphicSignatureTest extends PolymorphicSignatureTest {

  public EcjPolymorphicSignatureTest(@Nonnull String name) {
    super(name);
  }

  public static class MyAdapter extends JUnit4TestAdapter {

    public MyAdapter(Class<?> newTestClass) {
      super(newTestClass);
      try {
        filter(new Filter() {
          @Override
          public boolean shouldRun(Description description) {
            return true;
          }

          @Override
          public String describe() {
            return "EcjPolymorphicSignatureTest";
          }
        });
      } catch (NoTestsRemainException e) {
        Assert.fail();
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    // No need to do setup, it decreases loaded classes.
  }

  @Override
  protected void tearDown() throws Exception {
    // No need to do tearDown, it decreases loaded classes.
  }

  public static Test suite() {
   return new MyAdapter(EcjPolymorphicSignatureTest.class);
  }

  //Compile and run source file
  @Override
  public void runConformTest(String[] srcDescription, String expectedResult) {
    try {
      File dexOutDir = AbstractTestTools.createTempDir();
      File sourceFolder = buildSourceFolder(srcDescription);

      // Build dex file
      JackBasedToolchain jackToolchain =
          createToolchain(Collections.<Class<? extends IToolchain>>emptyList());
      jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);

      File dexFile = new File(dexOutDir, "classes.dex");

      List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
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

  @Nonnull
  protected JackBasedToolchain createToolchain(
      @Nonnull List<Class<? extends IToolchain>> excludeList) throws AssumptionViolatedException {
    JackBasedToolchain jackToolchain = null;
    jackToolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);

    File[] bootclasspath = jackToolchain.getDefaultBootClasspath();
    jackToolchain.addToClasspath(bootclasspath);
    jackToolchain.setSourceLevel(SourceLevel.JAVA_7);

    return jackToolchain;
  }
}
