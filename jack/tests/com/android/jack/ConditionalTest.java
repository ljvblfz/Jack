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

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.ThreadConfig;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of conditional test.
 */
public class ConditionalTest {

  @Nonnull
  private static RunnableHooks hooks;

  @BeforeClass
  public static void setUpClass() throws ConfigurationException, IllegalOptionsException {
    Options options = new Options();
    hooks = new RunnableHooks();
    options.checkValidity(hooks);
    options.getConfigBuilder(hooks).getCodecContext().setDebug();
    ThreadConfig.setConfig(options.getConfig());
  }

  @AfterClass
  public static void tearDownClass() {
    hooks.runHooks();
    ThreadConfig.unsetConfig();
  }

  @Test
  public void testCompile6() throws Exception {
    Options options = new Options();
    RunnableHooks hooks = new RunnableHooks();
    options.checkValidity(hooks);
    try {
      ThreadConfig.setConfig(options.getConfig());
      JSession session = Jack.getSession();
      JPhantomLookup lookup = session.getPhantomLookup();

      JPackage p = new JPackage("test", null);
      JDefinedClass classTest = new JDefinedClass(SourceInfo.UNKNOWN, "Test", JModifier.PUBLIC, p,
          NopClassOrInterfaceLoader.INSTANCE);
      JMethod method = new JMethod(SourceInfo.UNKNOWN,
          new JMethodId(new JMethodIdWide("test", MethodKind.STATIC),
              JPrimitiveTypeEnum.VOID.getType()),
          classTest,
          JModifier.PUBLIC | JModifier.STATIC);

      JArrayType arrayInt = lookup.getArrayType(JPrimitiveTypeEnum.INT.getType(), 1);
      JArrayType arrayByte = lookup.getArrayType(JPrimitiveTypeEnum.BYTE.getType(), 1);
      JArrayType arrayIntInt = lookup.getArrayType(JPrimitiveTypeEnum.INT.getType(), 2);
      JArrayType arrayIntIntInt = lookup.getArrayType(JPrimitiveTypeEnum.INT.getType(), 3);
      JClass testClass = lookup.getClass("Lcom/android/Test;");
      JArrayType arrayTest = lookup.getArrayType(testClass, 1);
      JArrayType arrayTestTest = lookup.getArrayType(testClass, 2);
      JArrayType arrayTestTestTest = lookup.getArrayType(testClass, 3);

      JParameter pArrayInt = new JParameter(SourceInfo.UNKNOWN, "pArrayInt", arrayInt, 0, method);
      JParameter pArrayIntInt =
          new JParameter(SourceInfo.UNKNOWN, "pArrayIntInt", arrayIntInt, 0, method);
      JParameter pArrayIntIntInt =
          new JParameter(SourceInfo.UNKNOWN, "pArrayIntIntInt", arrayIntIntInt, 0, method);
      JParameter pArrayByte = new JParameter(SourceInfo.UNKNOWN, "pArrayByte", arrayByte, 0, method);
      JClass javaLangObject =
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JParameter pObject =
          new JParameter(SourceInfo.UNKNOWN, "pArrayByte", javaLangObject, 0,
              method);
      JParameter pArrayTest = new JParameter(SourceInfo.UNKNOWN, "pArrayTest", arrayTest, 0, method);
      JParameter pArrayTestTest =
          new JParameter(SourceInfo.UNKNOWN, "pArrayTestTest", arrayTestTest, 0, method);
      JParameter pArrayTestTestTest =
          new JParameter(SourceInfo.UNKNOWN, "pArrayTestTestTest", arrayTestTestTest, 0, method);

      {
        // true ? int[] : byte[] => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayInt.makeRef(SourceInfo.UNKNOWN),
                pArrayByte.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }

      {
        // true ? int[] : Object => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayInt.makeRef(SourceInfo.UNKNOWN),
                pObject.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }

      {
        // true ? int[][] : int[] => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayIntInt.makeRef(SourceInfo.UNKNOWN),
                pArrayInt.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }

      {
        // true ? int[] : int[] => int []
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayInt.makeRef(SourceInfo.UNKNOWN),
                pArrayInt.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(arrayInt, conditional.getType());
      }

      {
        // true ? int[][] : int[][][] => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayIntInt.makeRef(SourceInfo.UNKNOWN),
                pArrayIntIntInt.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }

      {
        // true ? Test[] : Test[][] => Object[]
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayTest.makeRef(SourceInfo.UNKNOWN),
                pArrayTestTest.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(lookup.getArrayType(javaLangObject, 1),
            conditional.getType());
      }

      {
        // true ? Test[][] : Test[][][] => Object[][]
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayTestTest.makeRef(SourceInfo.UNKNOWN),
                pArrayTestTestTest.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(
            lookup.getArrayType(javaLangObject, 2),
            conditional.getType());
      }

      {
        // true ? int[][] : Test[][] => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayIntInt.makeRef(SourceInfo.UNKNOWN),
                pArrayTestTest.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }

      {
        // true ? int[][] : Test[][][] => Object
        JConditionalExpression conditional =
            new JConditionalExpression(SourceInfo.UNKNOWN, new JBooleanLiteral(
                SourceInfo.UNKNOWN, true), pArrayIntInt.makeRef(SourceInfo.UNKNOWN),
                pArrayTestTestTest.makeRef(SourceInfo.UNKNOWN));
        Assert.assertEquals(javaLangObject, conditional.getType());
      }
    } finally {
      hooks.runHooks();
    }
  }
}
