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

import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;

import junit.framework.Assert;

import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of static field access.
 */
public class UnaryTest {

  @Test
  public void testCompile5() throws Exception {
    String classBinaryName = "com/android/jack/unary/test005/jack/UnaryNot";
    String methodSignature = "getValue1(II)I";
    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);

    Assert.assertNotNull(m);
    CountIfStatement cis = new CountIfStatement();
    cis.accept(m);
    Assert.assertEquals(1, cis.countIf);

    methodSignature = "getValue2(IIII)I";
    m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);

    Assert.assertNotNull(m);
    cis = new CountIfStatement();
    cis.accept(m);
    Assert.assertEquals(2, cis.countIf);

  }

  private static class CountIfStatement extends JVisitor {
    private int countIf = 0;

    @Override
    public boolean visit(@Nonnull JIfStatement x) {
      countIf++;
      return super.visit(x);
    }
  }
}
