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

package com.android.jack.java8;

import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public class EcjLambdaTestPostM extends EcjLambdaTest {

  public EcjLambdaTestPostM(@Nonnull String name) {
    super(name);
  }

  public static final List<String> testForNewRuntime =
      Arrays.asList("testReferenceExpressionInference1",
          "testReferenceExpressionInference2",
          "testReferenceExpressionInference3a",
          "test425152",
          "test427744",
          "test431514",
          "test431514a",
          "test421712",
          "test406744d",
          "test027");

  public static class MyAdapter extends JUnit4TestAdapter {

    public MyAdapter(Class<?> newTestClass) {
      super(newTestClass);
      try {
        filter(new Filter() {
          @Override
          public boolean shouldRun(Description description) {
            return testForNewRuntime.contains(description.getMethodName());
          }

          @Override
          public String describe() {
            return "EcjLambdaTestForNewRuntime";
          }
        });
      } catch (NoTestsRemainException e) {
        Assert.fail();
      }
    }
  }

  public static Test suite() {
    return new MyAdapter(EcjLambdaTestPostM.class);
   }

  @Override
  protected JackBasedToolchain createToolchain() throws AssumptionViolatedException {
    JackBasedToolchain jackToolchain = super.createToolchain();
    jackToolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidCompatibilityChecker.N_API_LEVEL));
    return jackToolchain;
  }

}

