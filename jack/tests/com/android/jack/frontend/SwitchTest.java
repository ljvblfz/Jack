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

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.util.filter.SignatureMethodFilter;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class SwitchTest {
  @Nonnull
  private static final String CLASS_BINARY_NAME = "com/android/jack/switchstatement/test005/jack/Switch";

  @Nonnull
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";

  @Nonnull
  private static final File FILE = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void testCompileSwitch001() throws Exception {
    final String methodSignature = "switch001(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertEquals(true, firstStmt instanceof JSwitchStatement);
    JSwitchStatement switchStmt = (JSwitchStatement) firstStmt;
    Assert.assertEquals(true, switchStmt.getDefaultCase() != null);
    Assert.assertEquals(2, switchStmt.getCases().size());
  }

  @Test
  public void testCompileSwitch002() throws Exception {
    final String methodSignature = "switch002(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertEquals(true, firstStmt instanceof JSwitchStatement);
    JSwitchStatement switchStmt = (JSwitchStatement) firstStmt;
    Assert.assertEquals(true, switchStmt.getDefaultCase() == null);
    Assert.assertEquals(2, switchStmt.getCases().size());

    JStatement thirdStmt = switchStmt.getBody().getStatements().get(3);
    Assert.assertEquals(true, thirdStmt instanceof JBlock);

    firstStmt = ((JBlock) thirdStmt).getStatements().get(0);
    Assert.assertEquals(true, firstStmt instanceof JSwitchStatement);
    switchStmt = (JSwitchStatement) firstStmt;
    Assert.assertEquals(true, switchStmt.getDefaultCase() != null);
    Assert.assertEquals(1, switchStmt.getCases().size());
  }

  @Test
  public void testCompileSwitch003() throws Exception {
    final String methodSignature = "switch003(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertTrue(firstStmt instanceof JSwitchStatement);
  }

  @Test
  public void testCompileSwitch004() throws Exception {
    final String methodSignature = "switch004(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertTrue(firstStmt instanceof JSwitchStatement);
  }

}
