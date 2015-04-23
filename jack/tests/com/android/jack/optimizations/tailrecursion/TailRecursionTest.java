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

package com.android.jack.optimizations.tailrecursion;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.util.filter.SignatureMethodFilter;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class TailRecursionTest {

  private JMethod getJMethodAfterTailRecursionOptimization(@Nonnull File fileName,
      @Nonnull String className, @Nonnull String methodSignature) throws Exception {
    Options commandLineArgs = TestTools.buildCommandLineArgs(fileName);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);
    commandLineArgs.addProperty(Options.DROP_METHOD_BODY.getName(), "false");
    commandLineArgs.addProperty(Options.OPTIMIZE_TAIL_RECURSION.getName(), "true");
    return TestTools.getJMethodWithCommandLineArgs(commandLineArgs, className, methodSignature);
  }

  @Test
  public void tailRecursion001() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/tailrecursion/test001/jack/TailRecursion";
    String methodSignature = "test001(II)I";

    JMethod m =
        getJMethodAfterTailRecursionOptimization(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    StatementsCounter counter1 = new StatementsCounter(JReturnStatement.class);
    counter1.accept(m);
    Assert.assertEquals(1, counter1.getCounter());
    StatementsCounter counter2 = new StatementsCounter(JGoto.class);
    counter2.accept(m);
    Assert.assertEquals(1, counter2.getCounter());
  }

  private static class StatementsCounter extends JVisitor {

    private int counter = 0;

    @Nonnull
    private final Class<? extends JStatement> clazz;

    public StatementsCounter(Class<? extends JStatement> clazz) {
      this.clazz = clazz;
    }

    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      if (clazz.isInstance(stmt)) {
        counter++;
      }
      return super.visit(stmt);
    }

    public int getCounter() {
      return counter;
    }
  }

}

