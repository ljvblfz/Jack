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

import com.android.jack.Options;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrefixBitNotOperation;
import com.android.jack.ir.ast.JPrefixNegOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.optimizations.ExpressionSimplifier.Simplifier;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ThreadConfig;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

public class ExpressionSimplifierTest {

  @Nonnull
  private final JDefinedClass classTest ;

  @Nonnull
  private final JMethod method ;

  @Nonnull
  private final JParameter param;

  public ExpressionSimplifierTest() {
    JPackage p = new JPackage("test", new JSession(), null);
    classTest = new JDefinedClass(SourceOrigin.UNKNOWN, "Test", JModifier.PUBLIC, p,
        NopClassOrInterfaceLoader.INSTANCE);
    method =
        new JMethod(SourceOrigin.UNKNOWN, new JMethodId("test", MethodKind.STATIC), classTest,
            JPrimitiveTypeEnum.VOID.getType(), JModifier.PUBLIC | JModifier.STATIC);
    param = new JParameter(SourceOrigin.UNKNOWN, "p", JPrimitiveTypeEnum.BOOLEAN.getType(), 0, method);
    method.addParam(param);
    JBlock bodyBlock = new JBlock(SourceOrigin.UNKNOWN);
    method.setBody(new JMethodBody(SourceOrigin.UNKNOWN, bodyBlock));

  }
  @BeforeClass
  public static void setUp() throws Exception {
    ExpressionSimplifierTest.class.getClassLoader().setDefaultAssertionStatus(true);
    Options options = new Options();
    options.checkValidity(new RunnableHooks());
    options.getConfigBuilder().setDebug();
    ThreadConfig.setConfig(options.getConfig());
  }

  @Nonnull
  private JExpression getSimplifiedExpresssion(@Nonnull JExpression exprToSimplify) {
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JBlock bodyBlock = body.getBlock();
    bodyBlock.clear();
    TransformationRequest tr = new TransformationRequest(method);
    tr.append(new AppendStatement(bodyBlock, exprToSimplify.makeStatement()));
    tr.commit();

    Simplifier s = new ExpressionSimplifier.Simplifier();
    s.accept(method);

    Assert.assertEquals(1, bodyBlock.getStatements().size());
    JStatement stmt = bodyBlock.getStatements().get(0);
    Assert.assertTrue(stmt instanceof JExpressionStatement);
    JExpression simplifiedExpr = ((JExpressionStatement) stmt).getExpr();
    Assert.assertNotNull(simplifiedExpr);

    return simplifiedExpr;
  }

  @Test
  public void simplifyNumberComparisons() {
    JBinaryOperator[] opsToTest = new JBinaryOperator[] {
        JBinaryOperator.EQ,
        JBinaryOperator.NEQ,
        JBinaryOperator.LT,
        JBinaryOperator.LTE,
        JBinaryOperator.GT,
        JBinaryOperator.GTE};

    JExpression[][] operands = new JExpression[][] {
        {new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JIntLiteral(SourceOrigin.UNKNOWN, 2)},
        {new JIntLiteral(SourceOrigin.UNKNOWN, 7), new JIntLiteral(SourceOrigin.UNKNOWN, 5)},
        {new JIntLiteral(SourceOrigin.UNKNOWN, 6), new JIntLiteral(SourceOrigin.UNKNOWN, 6)},
        {new JFloatLiteral(SourceOrigin.UNKNOWN, 7.6f), new JFloatLiteral(SourceOrigin.UNKNOWN, 6.2f)},
        {new JDoubleLiteral(SourceOrigin.UNKNOWN, 7.6), new JDoubleLiteral(SourceOrigin.UNKNOWN, 6.2)},
        {new JLongLiteral(SourceOrigin.UNKNOWN, 7L), new JLongLiteral(SourceOrigin.UNKNOWN, 6L)},
        {new JShortLiteral(SourceOrigin.UNKNOWN, (short) 7), new JShortLiteral(SourceOrigin.UNKNOWN, (short) 6)},
        {new JIntLiteral(SourceOrigin.UNKNOWN, 7),
                new JDynamicCastOperation(SourceOrigin.UNKNOWN, JPrimitiveTypeEnum.INT.getType(),
                    new JFloatLiteral(SourceOrigin.UNKNOWN, 7.1f))},
        };

    boolean[][] results = new boolean[][] {
        // operator EQ
        {false, false, true, false, false, false, false, true},
        // operator NEQ
        {true, true, false, true, true, true, true, false},
        // operator LT
        {true, false, false, false, false, false, false, false},
        // operator LTE
        {true, false, true, false, false, false, false, true},
        // operator GT
        {false, true, false, true, true, true, true, false},
        // operator GTE
        {false, true, true, true, true, true, true, true}
    };


    for (int opIdx = 0; opIdx < opsToTest.length; opIdx++) {
      JBinaryOperator opToTest = opsToTest[opIdx];
      for (int operandIdx = 0; operandIdx < operands.length; operandIdx++) {
        JExpression[] operand = operands[operandIdx];
        boolean result = results[opIdx][operandIdx];

        // operands[0] op operands[1]
        JExpression simplifiedExpr =
            getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, opToTest,
                operand[0], operand[1]));
        Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
        if (result) {
          Assert.assertTrue(((JBooleanLiteral) simplifiedExpr).getValue());
        } else {
          Assert.assertFalse(((JBooleanLiteral) simplifiedExpr).getValue());
        }
      }
    }
  }

  @Test
  public void simplifyArithmeticWithIntResult() {
    JBinaryOperator[] opsToTest = new JBinaryOperator[] {
        JBinaryOperator.ADD,
        JBinaryOperator.SUB,
        JBinaryOperator.MUL,
        JBinaryOperator.DIV,
        JBinaryOperator.MOD,
        JBinaryOperator.SHL,
        JBinaryOperator.SHR,
        JBinaryOperator.SHRU,
        JBinaryOperator.BIT_AND,
        JBinaryOperator.BIT_OR,
        JBinaryOperator.BIT_XOR};

    JExpression[][] operands = new JExpression[][] {
        {new JIntLiteral(SourceOrigin.UNKNOWN, 7), new JIntLiteral(SourceOrigin.UNKNOWN, 5)},
        {new JIntLiteral(SourceOrigin.UNKNOWN, 100), new JIntLiteral(SourceOrigin.UNKNOWN, 5)},
        {new JIntLiteral(SourceOrigin.UNKNOWN, 7),
                new JDynamicCastOperation(SourceOrigin.UNKNOWN, JPrimitiveTypeEnum.INT.getType(),
                    new JDoubleLiteral(SourceOrigin.UNKNOWN, 3.2))},
        };

    int[][] results = new int[][] {
        // operator ADD
        {7 + 5, 100 + 5, 7 + (int) 3.2},
        // operator SUB
        {7 - 5, 100 - 5, 7 - (int) 3.2},
        // operator MUL
        {7 * 5, 100 * 5, 7 * (int) 3.2},
        // operator DIV
        {7 / 5, 100 / 5, 7 / (int) 3.2},
        // operator MOD
        {7 % 5, 100 % 5, 7 % (int) 3.2},
        // operator SHL
        {7 << 5, 100 << 5, 7 << (int) 3.2},
        // operator SHR
        {7 >> 5, 100 >> 5, 7 >> (int) 3.2},
        // operator SHRU
        {7 >>> 5, 100 >>> 5, 7 >>> (int) 3.2},
        // operator BIT_AND
        {7 & 5, 100 & 5, 7 & (int) 3.2},
        // operator BIT_OR
        {7 | 5, 100 | 5, 7 | (int) 3.2},
        // operator BIT_XOR
        {7 ^ 5, 100 ^ 5, 7 ^ (int) 3.2},
    };

    for (int opIdx = 0; opIdx < opsToTest.length; opIdx++) {
      JBinaryOperator opToTest = opsToTest[opIdx];
      for (int operandIdx = 0; operandIdx < operands.length; operandIdx++) {
        JExpression[] operand = operands[operandIdx];
        int result = results[opIdx][operandIdx];

        // operands[0] op operands[1]
        JExpression simplifiedExpr =
            getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, opToTest,
                operand[0], operand[1]));
        Assert.assertTrue(simplifiedExpr instanceof JIntLiteral);
        Assert.assertEquals(result , ((JIntLiteral) simplifiedExpr).getValue());
      }
    }
  }

  @Test
  public void simplifyArithmeticWithFloatResult() {
    JBinaryOperator[] opsToTest = new JBinaryOperator[] {
        JBinaryOperator.ADD,
        JBinaryOperator.SUB,
        JBinaryOperator.MUL,
        JBinaryOperator.DIV,
        JBinaryOperator.MOD};

    JExpression[][] operands = new JExpression[][] {
        {new JFloatLiteral(SourceOrigin.UNKNOWN, 5.2f), new JFloatLiteral(SourceOrigin.UNKNOWN, 3.5f)},
        {new JFloatLiteral(SourceOrigin.UNKNOWN, 100.100f), new JFloatLiteral(SourceOrigin.UNKNOWN, 5)},
        {new JFloatLiteral(SourceOrigin.UNKNOWN, 7.6f),
                new JDynamicCastOperation(SourceOrigin.UNKNOWN, JPrimitiveTypeEnum.FLOAT.getType(),
                    new JIntLiteral(SourceOrigin.UNKNOWN, 15))},
        };

    float[][] results = new float[][] {
        // operator ADD
        {5.2f + 3.5f, 100.100f + 5f, 7.6f + 15},
        // operator SUB
        {5.2f - 3.5f, 100.100f - 5f, 7.6f - 15},
        // operator MUL
        {5.2f * 3.5f, 100.100f * 5f, 7.6f * 15},
        // operator DIV
        {5.2f / 3.5f, 100.100f / 5f, 7.6f / 15},
        // operator MOD
        {5.2f % 3.5f, 100.100f % 5f, 7.6f % 15},
    };

    for (int opIdx = 0; opIdx < opsToTest.length; opIdx++) {
      JBinaryOperator opToTest = opsToTest[opIdx];
      for (int operandIdx = 0; operandIdx < operands.length; operandIdx++) {
        JExpression[] operand = operands[operandIdx];
        float result = results[opIdx][operandIdx];

        // operands[0] op operands[1]
        JExpression simplifiedExpr =
            getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, opToTest,
                operand[0], operand[1]));
        Assert.assertTrue(simplifiedExpr instanceof JFloatLiteral);
        Assert.assertEquals(result , ((JFloatLiteral) simplifiedExpr).getValue(), 0);
      }
    }
  }

  @Test
  public void simplifyCast() {
    // (int) 1.5f
    JExpression simplifiedExpr =
        getSimplifiedExpresssion(new JDynamicCastOperation(SourceOrigin.UNKNOWN,
            JPrimitiveTypeEnum.INT.getType(), new JFloatLiteral(SourceOrigin.UNKNOWN, 1.5f)));
    Assert.assertTrue(simplifiedExpr instanceof JIntLiteral);
    Assert.assertEquals(1, ((JIntLiteral) simplifiedExpr).getIntValue());


    // (byte) 1.5f
    simplifiedExpr =
        getSimplifiedExpresssion(new JDynamicCastOperation(SourceOrigin.UNKNOWN,
            JPrimitiveTypeEnum.BYTE.getType(), new JFloatLiteral(SourceOrigin.UNKNOWN, 1.5f)));
    Assert.assertTrue(simplifiedExpr instanceof JByteLiteral);
    Assert.assertEquals(1, ((JByteLiteral) simplifiedExpr).getIntValue());

    // (short) 2.9
    simplifiedExpr =
        getSimplifiedExpresssion(new JDynamicCastOperation(SourceOrigin.UNKNOWN,
            JPrimitiveTypeEnum.SHORT.getType(), new JDoubleLiteral(SourceOrigin.UNKNOWN, 2.9)));
    Assert.assertTrue(simplifiedExpr instanceof JShortLiteral);
    Assert.assertEquals(2, ((JShortLiteral) simplifiedExpr).getIntValue());


    // (integer) 1
    simplifiedExpr =
        getSimplifiedExpresssion(new JDynamicCastOperation(SourceOrigin.UNKNOWN,
            JPrimitiveTypeEnum.INT.getType(), new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1)));
    Assert.assertTrue(simplifiedExpr instanceof JIntLiteral);
    Assert.assertEquals(1, ((JIntLiteral) simplifiedExpr).getIntValue());

    // (long) 4.1
    simplifiedExpr =
        getSimplifiedExpresssion(new JDynamicCastOperation(SourceOrigin.UNKNOWN,
            JPrimitiveTypeEnum.LONG.getType(), new JDoubleLiteral(SourceOrigin.UNKNOWN, 4.1)));
    Assert.assertTrue(simplifiedExpr instanceof JLongLiteral);
    Assert.assertEquals(4, ((JLongLiteral) simplifiedExpr).getValue());
  }

  @Test
  public void simplifyConditional() {
    JExpression[] expressions = new JExpression[] {
        new JConditionalExpression(SourceOrigin.UNKNOWN, new JBooleanLiteral(
            SourceOrigin.UNKNOWN, true), new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JIntLiteral(
            SourceOrigin.UNKNOWN, 2)),
        new JConditionalExpression(SourceOrigin.UNKNOWN, new JBooleanLiteral(
            SourceOrigin.UNKNOWN, false), new JIntLiteral(SourceOrigin.UNKNOWN, 1),
            new JIntLiteral(SourceOrigin.UNKNOWN, 2)),
        new JConditionalExpression(SourceOrigin.UNKNOWN, JBinaryOperation.create(
            SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JIntLiteral(SourceOrigin.UNKNOWN, 8),
            JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.ADD, new JIntLiteral(
                SourceOrigin.UNKNOWN, 3), new JIntLiteral(SourceOrigin.UNKNOWN, 5))),
            new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JIntLiteral(SourceOrigin.UNKNOWN, 2))
    };

    int[] results = new int[] {1, 2, 1};

    int resultIdx = 0;
    for (JExpression expr : expressions) {
      int result = results[resultIdx++];

      JExpression simplifiedExpr = getSimplifiedExpresssion(expr);
      Assert.assertTrue(simplifiedExpr instanceof JIntLiteral);
      Assert.assertEquals(result, ((JIntLiteral) simplifiedExpr).getValue(), 0);
    }
  }

  @Test
  public void simplifyUnaryWithIntResult() {
    JExpression[] expressions = new JExpression[] {
        new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 7)),
        new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 100)),
        new JPrefixBitNotOperation(SourceOrigin.UNKNOWN,new JIntLiteral(SourceOrigin.UNKNOWN, -100)),
        new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JDynamicCastOperation(
            SourceOrigin.UNKNOWN, JPrimitiveTypeEnum.INT.getType(), new JDoubleLiteral(SourceOrigin.UNKNOWN,
                3.2))),
        new JPrefixNegOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 7)),
        new JPrefixNegOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 100)),
        new JPrefixNegOperation(SourceOrigin.UNKNOWN,new JIntLiteral(SourceOrigin.UNKNOWN, -100)),
        new JPrefixNegOperation(SourceOrigin.UNKNOWN, new JDynamicCastOperation(
            SourceOrigin.UNKNOWN, JPrimitiveTypeEnum.INT.getType(), new JDoubleLiteral(SourceOrigin.UNKNOWN,
                3.2))),
        };

    int[] results = new int[] {
        ~7, ~100, ~-100, ~ (int) 3.2,
        -7, -100, -(-100), - (int) 3.2
    };

    int resultIdx = 0;
    for (JExpression expr : expressions) {
      int result = results[resultIdx++];

      JExpression simplifiedExpr = getSimplifiedExpresssion(expr);
      Assert.assertTrue(simplifiedExpr instanceof JIntLiteral);
      Assert.assertEquals(result, ((JIntLiteral) simplifiedExpr).getValue());
    }
  }

  @Test
  @SuppressWarnings("all")
  public void simplifyUnaryWithBooleanResult() {
    JExpression[] expressions = new JExpression[] {
        new JPrefixNotOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true)),
        new JPrefixNotOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, false)),
        new JPrefixNotOperation(SourceOrigin.UNKNOWN, JBinaryOperation.create(
            SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JIntLiteral(SourceOrigin.UNKNOWN, 8),
            JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.ADD, new JIntLiteral(
                SourceOrigin.UNKNOWN, 3), new JIntLiteral(SourceOrigin.UNKNOWN, 5)))),
        new JPrefixNotOperation(SourceOrigin.UNKNOWN, JBinaryOperation.create(
            SourceOrigin.UNKNOWN, JBinaryOperator.NEQ, new JIntLiteral(SourceOrigin.UNKNOWN, 8),
            JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.ADD, new JIntLiteral(
                SourceOrigin.UNKNOWN, 3), new JIntLiteral(SourceOrigin.UNKNOWN, 5))))
        };

    boolean[] results = new boolean[] {
        !true, !false, !(8 ==  3 + 5), !(8 != 3 + 5)
    };

    int resultIdx = 0;
    for (JExpression expr : expressions) {
      boolean result = results[resultIdx++];

      JExpression simplifiedExpr = getSimplifiedExpresssion(expr);
      Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
      if (result) {
        Assert.assertTrue(((JBooleanLiteral) simplifiedExpr).getValue());
      } else {
        Assert.assertFalse(((JBooleanLiteral) simplifiedExpr).getValue());
      }
    }
  }

  @Test
  public void simplifyAndOr() {
    // param && true => param
    JExpression simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.AND,
            new JParameterRef(SourceOrigin.UNKNOWN, param), new JBooleanLiteral(
                SourceOrigin.UNKNOWN, true)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // param && false => param && false
    JBinaryOperation binExpr = JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.AND,
        new JParameterRef(SourceOrigin.UNKNOWN, param), new JBooleanLiteral(
            SourceOrigin.UNKNOWN, false));
    simplifiedExpr = getSimplifiedExpresssion(binExpr);
    Assert.assertTrue(simplifiedExpr instanceof JBinaryOperation);
    Assert.assertEquals(binExpr, simplifiedExpr);

    // param || true => param || true
    binExpr = JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.OR,
        new JParameterRef(SourceOrigin.UNKNOWN, param), new JBooleanLiteral(
            SourceOrigin.UNKNOWN, true));
    simplifiedExpr = getSimplifiedExpresssion(binExpr);
    Assert.assertTrue(simplifiedExpr instanceof JBinaryOperation);
    Assert.assertEquals(binExpr, simplifiedExpr);

    // param || false => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.OR,
            new JParameterRef(SourceOrigin.UNKNOWN, param), new JBooleanLiteral(
                SourceOrigin.UNKNOWN, false)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // false || param => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.OR,
            new JBooleanLiteral(SourceOrigin.UNKNOWN, false), new JParameterRef(
                SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // true || param => true
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.OR,
            new JBooleanLiteral(SourceOrigin.UNKNOWN, true), new JParameterRef(
                SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
    Assert.assertTrue(((JBooleanLiteral) simplifiedExpr).getValue());

    // false && param => false
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.AND,
            new JBooleanLiteral(SourceOrigin.UNKNOWN, false), new JParameterRef(
                SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
    Assert.assertFalse(((JBooleanLiteral) simplifiedExpr).getValue());

    // true && param => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.AND,
            new JBooleanLiteral(SourceOrigin.UNKNOWN, true), new JParameterRef(
                SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // true & param => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_AND, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
            new JParameterRef(SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // false | param => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_OR, new JBooleanLiteral(SourceOrigin.UNKNOWN, false),
            new JParameterRef(SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // false ^ param => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_XOR, new JBooleanLiteral(SourceOrigin.UNKNOWN, false),
            new JParameterRef(SourceOrigin.UNKNOWN, param)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // param ^ false => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_XOR, new JParameterRef(SourceOrigin.UNKNOWN, param),
            new JBooleanLiteral(SourceOrigin.UNKNOWN, false)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // param & true => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_AND, new JParameterRef(SourceOrigin.UNKNOWN, param),
            new JBooleanLiteral(SourceOrigin.UNKNOWN, true)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());

    // param | false => param
    simplifiedExpr =
        getSimplifiedExpresssion(JBinaryOperation.create(SourceOrigin.UNKNOWN,
            JBinaryOperator.BIT_OR, new JParameterRef(SourceOrigin.UNKNOWN, param),
            new JBooleanLiteral(SourceOrigin.UNKNOWN, false)));
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertEquals(param, ((JParameterRef) simplifiedExpr).getTarget());
  }

  @Test
  public void simplifyInstanceof() {
    JExpression[] expressions =
        new JExpression[] {
            new JInstanceOf(SourceOrigin.UNKNOWN, classTest, new JNullLiteral(SourceOrigin.UNKNOWN)),
            new JInstanceOf(SourceOrigin.UNKNOWN, classTest, new JDynamicCastOperation(
                SourceOrigin.UNKNOWN, classTest, new JNullLiteral(SourceOrigin.UNKNOWN)))};

    boolean[] results = new boolean[] {false, false};

    int resultIdx = 0;
    for (JExpression expr : expressions) {
      boolean result = results[resultIdx++];

      JExpression simplifiedExpr = getSimplifiedExpresssion(expr);
      Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
      if (result) {
        Assert.assertTrue(((JBooleanLiteral) simplifiedExpr).getValue());
      } else {
        Assert.assertFalse(((JBooleanLiteral) simplifiedExpr).getValue());
      }
    }
  }

  @Test
  public void simplifyIfCondition() {
    // Check that if (param == true) is simplify to if(param)
    JBinaryOperation binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JParameterRef(
            SourceOrigin.UNKNOWN, param), new JBooleanLiteral(SourceOrigin.UNKNOWN, true));

    JExpression simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertTrue(((JParameterRef) simplifiedExpr).getParameter() == param);

    // Check that if (param == false) is simplify to if(!param)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JParameterRef(
            SourceOrigin.UNKNOWN, param), new JBooleanLiteral(SourceOrigin.UNKNOWN, false));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JPrefixNotOperation);
    Assert.assertTrue(((JPrefixNotOperation) simplifiedExpr).getArg() instanceof JParameterRef);

    // Check that if (false == param) is simplify to if(!param)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JBooleanLiteral(
            SourceOrigin.UNKNOWN, false), new JParameterRef(SourceOrigin.UNKNOWN, param));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JPrefixNotOperation);
    Assert.assertTrue(((JPrefixNotOperation) simplifiedExpr).getArg() instanceof JParameterRef);

    // Check that if (true == param) is simplify to if(param)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.EQ, new JBooleanLiteral(
            SourceOrigin.UNKNOWN, true), new JParameterRef(SourceOrigin.UNKNOWN, param));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertTrue(((JParameterRef) simplifiedExpr).getParameter() == param);

    // Check that if (true || param) is simplify to if(true)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.OR, new JBooleanLiteral(
            SourceOrigin.UNKNOWN, true), new JParameterRef(SourceOrigin.UNKNOWN, param));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JBooleanLiteral);
    Assert.assertTrue(((JBooleanLiteral) simplifiedExpr).getValue());

    // Check that if (param != true) is simplify to if(!param)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.NEQ, new JParameterRef(
            SourceOrigin.UNKNOWN, param), new JBooleanLiteral(SourceOrigin.UNKNOWN, true));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JPrefixNotOperation);
    Assert.assertTrue(((JPrefixNotOperation) simplifiedExpr).getArg() instanceof JParameterRef);

    // Check that if (param != false) is simplify to if(param)
    binOp =
        JBinaryOperation.create(SourceOrigin.UNKNOWN, JBinaryOperator.NEQ, new JParameterRef(
            SourceOrigin.UNKNOWN, param), new JBooleanLiteral(SourceOrigin.UNKNOWN, false));

    simplifiedExpr = getSimplifiedExpressionIntoIf(binOp);
    Assert.assertTrue(simplifiedExpr instanceof JParameterRef);
    Assert.assertTrue(((JParameterRef) simplifiedExpr).getParameter() == param);
  }


  @Nonnull
  private JExpression getSimplifiedExpressionIntoIf(@Nonnull JBinaryOperation binOp) {
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    JBlock bodyBlock = body.getBlock();
    bodyBlock.clear();

    JIfStatement ifStmt =
        new JIfStatement(SourceOrigin.UNKNOWN, binOp, new JBlock(SourceOrigin.UNKNOWN), new JBlock(
            SourceOrigin.UNKNOWN));

    TransformationRequest tr = new TransformationRequest(method);
    tr.append(new AppendStatement(bodyBlock, ifStmt));
    tr.commit();

    Simplifier s = new ExpressionSimplifier.Simplifier();
    s.accept(method);

    Assert.assertEquals(1, bodyBlock.getStatements().size());
    JStatement stmt = bodyBlock.getStatements().get(0);
    Assert.assertTrue(stmt instanceof JIfStatement);
    JExpression simplifiedExpr = ((JIfStatement) stmt).getIfExpr();
    Assert.assertNotNull(simplifiedExpr);

    return simplifiedExpr;
  }
}
