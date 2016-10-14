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

import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.cfg.JConditionalBlockElement;
import com.android.jack.optimizations.Optimizations;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of static field access.
 */
public class UnaryTest {

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void testCompile5_getValue1() throws Exception {
    String classBinaryName = "com/android/jack/unary/test005/jack/UnaryNot";
    String methodSignature = "getValue1(II)I";

    Map<String, String> additionalProperty = new HashMap<String, String>();
    additionalProperty.put(Optimizations.NotSimplifier.ENABLE.getName(), "true");

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature, additionalProperty);

    Assert.assertNotNull(m);
    CountIfStatement cis = new CountIfStatement();
    cis.accept(m);
    Assert.assertEquals(1, cis.countIf);
  }

  @Test
  public void testCompile5_getValue2() throws Exception {
    String classBinaryName = "com/android/jack/unary/test005/jack/UnaryNot";
    String methodSignature = "getValue2(IIII)I";

    Map<String, String> additionalProperty = new HashMap<String, String>();
    additionalProperty.put(Optimizations.NotSimplifier.ENABLE.getName(), "true");
    additionalProperty.put(Optimizations.DefUseSimplifier.ENABLE.getName(), "true");
    additionalProperty.put(Optimizations.IfSimplifier.ENABLE.getName(), "true");
    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature, additionalProperty);

    Assert.assertNotNull(m);
    CountIfStatement cis = new CountIfStatement();
    cis.accept(m);
    Assert.assertEquals(2, cis.countIf);

  }

  private static class CountIfStatement extends JVisitor {
    private int countIf = 0;

    @Override
    public boolean visit(@Nonnull JConditionalBlockElement x) {
      countIf++;
      return super.visit(x);
    }
  }
}
