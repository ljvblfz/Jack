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

package com.android.jack.optimizations.inlinemethod;

import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.inlining.JMethodInliner;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexMethod;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeMethodsValidator;
import com.android.jack.test.dex.DexValidator;

import junit.framework.Assert;

import org.jf.dexlib.Code.Instruction;
import org.junit.Test;

/**
 * Sample a few {@link JMethodInliner} test case to make sure that the function are actually
 * inlined.
 */
public class InlineMethodTest extends DexOutputBasedTest {

  private class DexTypeInvokeValidator extends DexValidator<DexMethod> {
    private final int numCalls;

    private DexTypeInvokeValidator(int numCalls) {
      this.numCalls = numCalls;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    protected void validateImpl(DexMethod element) {
      int count = 0;
      for (Instruction i : element.getInstructions()) {
        switch (i.opcode) {
          case INVOKE_DIRECT:
          case INVOKE_DIRECT_EMPTY:
          case INVOKE_DIRECT_JUMBO:
          case INVOKE_DIRECT_RANGE:
          case INVOKE_VIRTUAL:
          case INVOKE_VIRTUAL_JUMBO:
          case INVOKE_VIRTUAL_QUICK:
          case INVOKE_VIRTUAL_QUICK_RANGE:
          case INVOKE_VIRTUAL_RANGE:
            count++;
        }
      }
      Assert.assertEquals(numCalls, count);
    }
  }

  @Test
  public void test001() throws Exception {
    String testPackage = "com.android.jack.optimizations.inlinemethod.test001";
    String annotationPackage = "com.android.jack.annotations";

    CompilationProperties properties = CompilationProperties.EMPTY
        .with(Optimizations.InlineAnnotatedMethods.ENABLE.getName(), Boolean.TRUE)
        .excludeJillToolchain();

    DexTypeInvokeValidator tv = new DexTypeInvokeValidator(0);

    DexTypeMethodsValidator mv = new DexTypeMethodsValidator().insert("callInlineMe01Once()V", tv);

    DexFileTypesValidator validators = new DexFileTypesValidator()
        .insert("Lcom/android/jack/optimizations/inlinemethod/test001/jack/TestCase;", mv);

    compileAndValidate(testPackage, properties, validators, annotationPackage);
  }

  @Test
  public void test002() throws Exception {
    String testPackage = "com.android.jack.optimizations.inlinemethod.test002";
    String annotationPackage = "com.android.jack.annotations";

    CompilationProperties properties = CompilationProperties.EMPTY
        .with(Optimizations.InlineAnnotatedMethods.ENABLE.getName(), Boolean.TRUE)
        .excludeJillToolchain();

    // If there are 3 calls, the body was inlined. Otherwise if we only have one call, @ForceInline
    // didn't copy in the body.
    DexTypeInvokeValidator tv = new DexTypeInvokeValidator(3);

    DexTypeMethodsValidator mv =
        new DexTypeMethodsValidator().insert("callInlineMe01NoCatch(I)I", tv);

    DexFileTypesValidator validators = new DexFileTypesValidator()
        .insert("Lcom/android/jack/optimizations/inlinemethod/test002/jack/TestCase;", mv);

    compileAndValidate(testPackage, properties, validators, annotationPackage);
  }

  @Test
  public void test003() throws Exception {
    String testPackage = "com.android.jack.optimizations.inlinemethod.test003";
    String annotationPackage = "com.android.jack.annotations";

    CompilationProperties properties = CompilationProperties.EMPTY
        .with(Optimizations.InlineAnnotatedMethods.ENABLE.getName(), Boolean.TRUE)
        .excludeJillToolchain();

    DexTypeInvokeValidator tv = new DexTypeInvokeValidator(0);

    DexTypeMethodsValidator mv =
        new DexTypeMethodsValidator().insert("callInlineMeDirectAccess(I)I", tv);

    DexFileTypesValidator validators = new DexFileTypesValidator()
        .insert("Lcom/android/jack/optimizations/inlinemethod/test003/jack/TestCase$S;", mv);

    compileAndValidate(testPackage, properties, validators, annotationPackage);
  }

  @Test
  public void test004() throws Exception {
    String testPackage = "com.android.jack.optimizations.inlinemethod.test004";
    String[] annotationPackage =
        new String[] {"com.android.jack.annotations",
                      "com.android.jack.optimizations.inlinemethod.test004"};

    CompilationProperties properties = CompilationProperties.EMPTY
        .with(Optimizations.InlineAnnotatedMethods.ENABLE.getName(), Boolean.TRUE)
        .excludeJillToolchain();

    DexTypeInvokeValidator tv = new DexTypeInvokeValidator(0);

    DexTypeMethodsValidator mv = new DexTypeMethodsValidator().insert("callAddOne(I)I", tv);

    DexFileTypesValidator validators = new DexFileTypesValidator()
        .insert("Lcom/android/jack/optimizations/inlinemethod/test004/jack/TestCase;", mv);

    compileAndValidate(testPackage, properties, validators, annotationPackage);
  }
}
