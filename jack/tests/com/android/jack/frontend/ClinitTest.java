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
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.util.NamingTools;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import java.util.List;

public class ClinitTest {

  public static final String CLASS_WITH_VARIABLE_INIT_BINARY_NAME
    = "com/android/jack/clinit/test001/jack/ClInitWithVariableInit";
  public static final String CLASS_BINARY_NAME
    = "com/android/jack/clinit/test001/jack/ClInit";

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void searchStaticInit() throws Exception {
    JMethod clinit = TestTools.getJMethodWithRejectAllFilter(
        TestTools.getJackTestFromBinaryName(CLASS_WITH_VARIABLE_INIT_BINARY_NAME),
        "L" + CLASS_WITH_VARIABLE_INIT_BINARY_NAME + ";",
        NamingTools.STATIC_INIT_NAME + "()V");

    JMethodBody body = (JMethodBody) clinit.getBody();
    assert body != null;
    List<JStatement> stmts = body.getStatements();
    Assert.assertEquals(2, stmts.size());
    Assert.assertTrue(stmts.get(0) instanceof JBlock);
    stmts = ((JBlock) stmts.get(0)).getStatements();
    Assert.assertEquals(1, stmts.size());
    JStatement firstStatement = stmts.get(0);
    Assert.assertTrue(firstStatement instanceof JExpressionStatement);
    Assert.assertTrue(((JExpressionStatement) firstStatement).getExpr() instanceof JAsgOperation);
 }

  @Test
  public void searchEmptyStaticInit() throws Exception {
    JMethod clinit = TestTools.getJMethodWithRejectAllFilter(
        TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME),
        "L" + CLASS_BINARY_NAME + ";", NamingTools.STATIC_INIT_NAME + "()V");

    Assert.assertEquals(1, ((JMethodBody) clinit.getBody()).getStatements().size());
  }
}
