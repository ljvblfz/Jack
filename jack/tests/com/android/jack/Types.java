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

import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JAndOperation;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLteOperation;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPrefixBitNotOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShlOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.CommonTypes;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for getType.
 */
public class Types {

  public Types() throws ConfigurationException, IllegalOptionsException, NothingToDoException {
    Options options = new Options();
    options.checkValidity(new RunnableHooks());
    options.getConfigBuilder().setDebug();
    ThreadConfig.setConfig(options.getConfig());

    JSession session = Jack.getSession();
    session.getLookup().getOrCreatePackage("java/lang");
  }

  @BeforeClass
  public static void setUpClass() {
    Types.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void unaryPrefixNotGetType() throws Exception {
    boolean catched;

    try {
      catched = false;
      new JPrefixNotOperation(SourceOrigin.UNKNOWN, new JLongLiteral(SourceOrigin.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // !long is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixNotOperation(
          SourceOrigin.UNKNOWN, new JShortLiteral(SourceOrigin.UNKNOWN, (short) 1)).getType();
    } catch (AssertionError e) {
      // !short is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JPrefixNotOperation(
        SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JPrefixNotOperation(
        SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l)).getType());
  }

  @Test
  public void unaryPrefixBitNotGetType() throws Exception {
    boolean catched;

    try {
      catched = false;
      new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JDoubleLiteral(SourceOrigin.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // ~double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JFloatLiteral(SourceOrigin.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // ~float is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixBitNotOperation(
          SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType();
    } catch (AssertionError e) {
      // ~boolean is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JPrefixBitNotOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l))
          .getType();
    } catch (AssertionError e) {
      // ~java.lang.Double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceOrigin.UNKNOWN, new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceOrigin.UNKNOWN, new JShortLiteral(SourceOrigin.UNKNOWN, (short) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JPrefixBitNotOperation(
        SourceOrigin.UNKNOWN, new JLongLiteral(SourceOrigin.UNKNOWN, 1l)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CHAR);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l)).getType());
  }

  @Test
  public void unaryPostfixIncGetType() throws Exception {
    boolean catched;

    try {
      catched = false;
      new JPostfixDecOperation(
          SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType();
    } catch (AssertionError e) {
      // boolean-- is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JPostfixDecOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l))
          .getType();
    } catch (AssertionError e) {
      // Boolean-- is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceOrigin.UNKNOWN, new JShortLiteral(SourceOrigin.UNKNOWN, (short) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.DOUBLE.getType(), new JPostfixDecOperation(
        SourceOrigin.UNKNOWN, new JDoubleLiteral(SourceOrigin.UNKNOWN, 1.0)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JPostfixDecOperation(
        SourceOrigin.UNKNOWN, new JLongLiteral(SourceOrigin.UNKNOWN, 1l)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l)).getType());
  }

  @Test
  public void shiftGetType() throws Exception {
    boolean catched;

    try {
      catched = false;
      new JShlOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean << int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JShlOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 1),
          new JFloatLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // int << float is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JShlOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Double << int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JShlOperation(SourceOrigin.UNKNOWN, new JIntLiteral(SourceOrigin.UNKNOWN, 1),
          new JLocalRef(SourceOrigin.UNKNOWN, l)).getType();
    } catch (AssertionError e) {
      // int << Double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
        new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
        new JLongLiteral(SourceOrigin.UNKNOWN, 1), new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
        new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1),
        new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
        new JCharLiteral(SourceOrigin.UNKNOWN, (char) 1),
        new JShortLiteral(SourceOrigin.UNKNOWN, (short) 1)).getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceOrigin.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l1))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_LONG);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceOrigin.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JShlOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l1))
          .getType());
    }
  }

  @Test
  public void relationalGetType() throws Exception {
    boolean catched;
    try {
      catched = false;
      new JLteOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean <= int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JLteOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
          new JFloatLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // ref object <= int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JIntLiteral(SourceOrigin.UNKNOWN, 1))
          .getType());
    }

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceOrigin.UNKNOWN,
        new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceOrigin.UNKNOWN,
        new JLongLiteral(SourceOrigin.UNKNOWN, 1), new JFloatLiteral(SourceOrigin.UNKNOWN, 1))
        .getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceOrigin.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l1))
          .getType());
    }
  }

  @Test
  public void logicalAndBitwiseGetType() throws Exception {
    boolean catched;
    try {
      catched = false;
      new JBitAndOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean & int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JBitAndOperation(SourceOrigin.UNKNOWN,
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JBitAndOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_INTEGER);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JBitAndOperation(SourceOrigin.UNKNOWN,
          new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_INTEGER);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JType t1 = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l1 = new JLocal(SourceOrigin.UNKNOWN, "test", t1, JModifier.DEFAULT, null);
      try {
        catched = false;
        new JBitAndOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
            new JLocalRef(SourceOrigin.UNKNOWN, l1)).getType();
      } catch (AssertionError e) {
        // Integer & Object is not valid
        catched = true;
      }
      Assert.assertTrue(catched);
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_FLOAT);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      try {
        catched = false;
        new JBitAndOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
            new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
      } catch (AssertionError e) {
        // Float & int is not valid
        catched = true;
      }
      Assert.assertTrue(catched);
    }

    try {
      catched = false;
      new JBitAndOperation(SourceOrigin.UNKNOWN, new JDoubleLiteral(SourceOrigin.UNKNOWN, 1),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // double & int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);
  }

  @Test
  public void equalityGetType() throws Exception {
    boolean catched;

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceOrigin.UNKNOWN,
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType());

    try {
      catched = false;
      new JEqOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean == int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceOrigin.UNKNOWN,
        new JIntLiteral(SourceOrigin.UNKNOWN, 1), new JIntLiteral(SourceOrigin.UNKNOWN, 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceOrigin.UNKNOWN,
        new JFloatLiteral(SourceOrigin.UNKNOWN, 1), new JIntLiteral(SourceOrigin.UNKNOWN, 1))
        .getType());

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JEqOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Object == int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceOrigin.UNKNOWN,
        new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l)).getType());
  }

  @Test
  public void conditionalOperationGetType() throws Exception {
    boolean catched;

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JAndOperation(SourceOrigin.UNKNOWN,
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
        new JBooleanLiteral(SourceOrigin.UNKNOWN, true)).getType());

    try {
      catched = false;
      new JAndOperation(SourceOrigin.UNKNOWN, new JBooleanLiteral(SourceOrigin.UNKNOWN, true),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean && int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JAndOperation(SourceOrigin.UNKNOWN,
        new JLocalRef(SourceOrigin.UNKNOWN, l), new JLocalRef(SourceOrigin.UNKNOWN, l)).getType());
  }

  @Test
  public void arithmeticGetType() throws Exception {
    boolean catched;
    JType objectType = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
    JLocal l = new JLocal(SourceOrigin.UNKNOWN, "test", objectType, JModifier.DEFAULT, null);

    try {
      catched = false;
      new JAddOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, l),
          new JIntLiteral(SourceOrigin.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Object add int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JAddOperation(SourceOrigin.UNKNOWN,
        new JByteLiteral(SourceOrigin.UNKNOWN, (byte) 1), new JIntLiteral(SourceOrigin.UNKNOWN, 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.DOUBLE.getType(), new JAddOperation(SourceOrigin.UNKNOWN,
        new JDoubleLiteral(SourceOrigin.UNKNOWN, 1), new JFloatLiteral(SourceOrigin.UNKNOWN, 1))
        .getType());
  }

  @Test
  public void test001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("type/test001")));
  }

  @Test
  public void test002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("type/test002")));
  }
}
