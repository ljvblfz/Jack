/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.optimizations;

import com.android.jack.TestTools;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JVisitor;

import junit.framework.Assert;

import org.junit.Test;

import javax.annotation.Nonnull;

public class NotSimplifierTest {

  @Test
  public void notRemover001() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test001(II)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.EQ).accept(m);
    new CheckAbsenceOfUnaryOp(JUnaryOperator.NOT).accept(m);
  }

  @Test
  public void notRemover002() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test002(II)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.LT).accept(m);
  }

  @Test
  public void notRemover003() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test003(IIII)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.LT).accept(m);
    new CheckAbsenceOfBinaryOp(JBinaryOperator.NEQ).accept(m);
  }

  @Test
  public void notRemover004() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test004(IIZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.LT).accept(m);
  }

  @Test
  public void notRemover005() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test005(IIII)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.LT).accept(m);
    new CheckAbsenceOfBinaryOp(JBinaryOperator.EQ).accept(m);
  }

  @Test
  public void notRemover006() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test006(II)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.NEQ).accept(m);
  }

  @Test
  public void notRemover007() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test007(II)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.EQ).accept(m);
  }

  @Test
  public void notRemover008() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test008(II)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.NEQ).accept(m);
  }

  @Test
  public void notRemover009() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test009(IIII)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.NEQ).accept(m);
  }

  @Test
  public void notRemover010() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test010(Z)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    Assert
        .assertTrue(((JMethodBody) m.getBody()).getBlock().getStatements().get(0)
            instanceof JIfStatement);
  }

  @Test
  public void notRemover011() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test011(ZZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    checkFirstStatement(m, JBinaryOperator.BIT_OR);
  }

  @Test
  public void notRemover012() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test012(ZZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    checkFirstStatement(m, JBinaryOperator.BIT_AND);
  }

  @Test
  public void notRemover013() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test013(ZZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    checkFirstStatement(m, JBinaryOperator.BIT_XOR);
  }

  @Test
  public void notRemover014() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test014(ZZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.BIT_AND).accept(m);
  }

  @Test
  public void notRemover015() throws Exception {
    String classBinaryName =
        "com/android/jack/optimizations/notsimplifier/test001/jack/NotSimplifier";
    String methodSignature = "test015(ZZ)Z";

    JMethod m =
        TestTools.getJMethodWithSignatureFilter(TestTools.getJackTestFromBinaryName(classBinaryName), "L"
            + classBinaryName + ";", methodSignature);
    Assert.assertNotNull(m);

    new CheckAbsenceOfBinaryOp(JBinaryOperator.BIT_OR).accept(m);
  }

  private void checkFirstStatement(@Nonnull JMethod m, @Nonnull JBinaryOperator expectedOp) {
    JMethodBody jMethodBody = (JMethodBody) m.getBody();
    Assert.assertNotNull(jMethodBody);
    JStatement firstStmt = jMethodBody.getBlock().getStatements().get(0);
    Assert.assertTrue(firstStmt instanceof JExpressionStatement);
    JExpression expr = ((JExpressionStatement) firstStmt).getExpr();
    Assert.assertTrue(expr instanceof JBinaryOperation);
    expr = ((JBinaryOperation) expr).getRhs();
    Assert.assertTrue(expr instanceof JBinaryOperation);
    Assert.assertTrue(((JBinaryOperation) expr).getOp() == expectedOp);
  }

  private static class CheckAbsenceOfBinaryOp extends JVisitor {

    @Nonnull
    private final JBinaryOperator absentOp;

    public CheckAbsenceOfBinaryOp(@Nonnull JBinaryOperator absentOp) {
      this.absentOp = absentOp;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binaryOp) {
      Assert.assertFalse(binaryOp.getOp() == absentOp);
      return super.visit(binaryOp);
    }
  }

  private static class CheckAbsenceOfUnaryOp extends JVisitor {

    @Nonnull
    private final JUnaryOperator absentOp;

    public CheckAbsenceOfUnaryOp(@Nonnull JUnaryOperator absentOp) {
      this.absentOp = absentOp;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unaryOp) {
      Assert.assertFalse(unaryOp.getOp() == absentOp);
      return super.visit(unaryOp);
    }
  }
}
