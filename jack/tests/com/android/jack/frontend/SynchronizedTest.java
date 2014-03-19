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
import com.android.jack.SignatureMethodFilter;
import com.android.jack.TestTools;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.TransformationRequest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

public class SynchronizedTest {
  @Nonnull
  private static final String CLASS_BINARY_NAME = "com/android/jack/synchronize/test002/jack/Synchronized";
  @Nonnull
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File FILE = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @Before
  public void setUp() throws Exception {
    StringTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testSynchronizedBlock() throws Exception {
    final String methodSignature = "sync(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.setFilter(new SignatureMethodFilter(methodSignature));

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertEquals(true, firstStmt instanceof JSynchronizedBlock);
    JSynchronizedBlock syncBlock = (JSynchronizedBlock) firstStmt;
    Assert.assertEquals(true,
        syncBlock.getSynchronizedBlock().getStatements().get(0) instanceof JReturnStatement);
    Assert.assertEquals(true, syncBlock.getLockExpr() instanceof JClassLiteral);
    }

  @Test
  public void testSynchronizedMethod() throws Exception {
    final String methodSignature = "syncMethod(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.setFilter(new SignatureMethodFilter(methodSignature));

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    Assert.assertTrue(method.isSynchronized());
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JStatement firstStmt = body.getBlock().getStatements().get(0);
    Assert.assertEquals(true, firstStmt instanceof JReturnStatement);
    }

  @Test
  public void testLockUnlock() throws Exception {
    final String methodSignature = "syncMethod(I)I";
    Options commandLineArgs = TestTools.buildCommandLineArgs(FILE);
    commandLineArgs.setFilter(new SignatureMethodFilter(methodSignature));

    JMethod method =
        FrontendTools.parseMethod(CLASS_SIGNATURE, methodSignature, commandLineArgs);

    Assert.assertTrue(method.isSynchronized());

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    List<JStatement> stmts = body.getBlock().getStatements();
    JStatement firstStmt = stmts.get(0);
    Assert.assertEquals(true, firstStmt instanceof JReturnStatement);
    SourceInfo srcInfo = firstStmt.getSourceInfo();

    TransformationRequest tr = new TransformationRequest(method);
    JClass javaLangClass = method.getParent(JProgram.class).getPhantomLookup()
        .getClass(CommonTypes.JAVA_LANG_CLASS);
    tr.append(new AppendBefore(firstStmt, new JLock(srcInfo,
        new JClassLiteral(srcInfo, method.getEnclosingType(), javaLangClass))));
    tr.append(new AppendBefore(firstStmt, new JUnlock(srcInfo,
        new JClassLiteral(srcInfo, method.getEnclosingType(), javaLangClass))));
    tr.commit();

    stmts = body.getBlock().getStatements();

    Assert.assertEquals(true,
        stmts.get(0) instanceof JLock);
    Assert.assertEquals(true,
        stmts.get(1) instanceof JUnlock);
    }
}
