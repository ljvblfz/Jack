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

package com.android.jack.cfg;


import com.android.jack.Options;
import com.android.jack.SignatureMethodFilter;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class BuildCfgTest {

  private List<BasicBlock> bbCodeOder;
  private static final String CLASS_BINARY_NAME = "com/android/jack/fibonacci/jack/FibonacciThreeAddress";

  @Before
  public void setUp() throws Exception {
    BuildCfgTest.class.getClassLoader().setDefaultAssertionStatus(true);

    String classSignature = "L" + CLASS_BINARY_NAME + ";";
    File fileName = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);
    final String methodSignature = "fibonacci(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(fileName);
    commandLineArgs.setFilter(new SignatureMethodFilter(methodSignature));

    JMethod method = CfgTools.buildCfg(classSignature, methodSignature, commandLineArgs);

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    bbCodeOder = cfg.getNodes();
  }

  @Test
  public void cfg001() throws Exception {
    Assert.assertEquals(6, bbCodeOder.size());
    Assert.assertEquals(true, bbCodeOder.get(1).getLastInstruction() instanceof JIfStatement);
    Assert.assertEquals(2, bbCodeOder.get(1).getSuccessors().size());
    JStatement lastInstruction = bbCodeOder.get(3).getLastInstruction();
    Assert.assertEquals(true,
        lastInstruction instanceof JExpressionStatement);
    Assert.assertTrue(
        ((JExpressionStatement) lastInstruction).getExpr() instanceof JAsgOperation);
    Assert.assertEquals(true,
        bbCodeOder.get(4).getLastInstruction() instanceof JExpressionStatement);
  }

  @Test
  public void cfg002() throws Exception {
    for (BasicBlock bb : bbCodeOder) {
      for (BasicBlock succ : bb.getSuccessors()) {
        Assert.assertTrue(succ.getPredecessors().contains(bb));
      }
    }
  }
}
