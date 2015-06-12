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

import static org.junit.Assert.fail;

import com.android.jack.TestTools;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.util.graph.GraphException;

import org.junit.Test;

public class BuildCfgErrorTest {

  private static final String CLASS_BINARY_NAME = "com/android/jack/fibonacci/test001/jack/FibonacciThreeAddress";

  /**
   * Add a successor twice to a block is not allow.
   * @throws Exception
   */
  @Test
  public void cfgError001() throws Exception {
    JMethod method =
        TestTools.getJMethodWithRejectAllFilter(
            TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME), "L" + CLASS_BINARY_NAME + ";",
            "fibonacci(I)I");
    ControlFlowGraph cfg = new ControlFlowGraph(method);
    NormalBasicBlock b0 = new NormalBasicBlock(cfg, BasicBlock.EMPTY_STATEMENT_LIST);
    NormalBasicBlock b1 = new NormalBasicBlock(cfg, BasicBlock.EMPTY_STATEMENT_LIST);
    b0.setTarget(b1);
    try {
      b0.setTarget(b1);
      fail();
    } catch (GraphException e) {
      // Exception is normal
    }
  }
}
