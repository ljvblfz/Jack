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

package com.android.jack.frontend;

import com.android.jack.TestTools;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.sourceinfo.SourceInfo;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Assume;
import org.junit.Test;

import java.util.List;

public class GotoTest {
  private static final String TEST_CLASS_BINARY_NAME = "com/android/jack/frontend/SynthetizeCode";

  @Test
  public void dumpGoto() throws Exception {

    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    JMethod dumpGoto = TestTools.getJMethodWithRejectAllFilter(
        TestTools.getJackUnitTestFromBinaryName(TEST_CLASS_BINARY_NAME),
        "L" + TEST_CLASS_BINARY_NAME + ";", "synthetizeCode()V");

    JMethodBody body = (JMethodBody) dumpGoto.getBody();
    assert body != null;
    List<JStatement> stmts = body.getStatements();
    Assert.assertEquals(1, stmts.size());
    Assert.assertTrue(stmts.get(0) instanceof JReturnStatement);
    JBlock block = body.getBlock();
    SourceInfo srcInfo = stmts.get(0).getSourceInfo();
    String label = "label";
    try {
    block.addStmt(0, new JGoto(SourceInfo.UNKNOWN, new JLabeledStatement(
        srcInfo, new JLabel(srcInfo, label), stmts.get(0))));
    Assert.fail();
    }
    catch (AssertionFailedError e) {
      // Thrown by Assert.fail(). This prevents this message from being caught
      // by next catch clause and thus being ignored.
      // This error may be caused by disabled assertions which can occur if this
      // test is ran with a test suite that use Jack compilation with disabled
      // sanity checks.
      Assert.fail("Warning: this test is only valid with assertions enabled. " +
          "You should check that it is case before considering there's a bug.");
    }
    catch (AssertionError e) {
      // Normal since goto target return instead of block containing return
    }
    JBlock gotoTargetBlock = new JBlock(srcInfo);
    gotoTargetBlock.addStmt(stmts.get(0));
    block.addStmt(0, new JGoto(SourceInfo.UNKNOWN, new JLabeledStatement(
        srcInfo, new JLabel(srcInfo, label), gotoTargetBlock)));
    stmts = body.getStatements();
    Assert.assertEquals(2, stmts.size());
    Assert.assertTrue(stmts.get(0) instanceof JGoto);
    Assert.assertEquals("goto "+label, stmts.get(0).toSource());
  }
}
