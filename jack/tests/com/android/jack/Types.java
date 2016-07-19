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
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for getType.
 */
public class Types {

  @Nonnull
  private static RunnableHooks hooks;

  @BeforeClass
  public static void setUp() throws ConfigurationException, IllegalOptionsException {
    Options options = new Options();
    hooks = new RunnableHooks();
    options.checkValidity(hooks);
    options.getConfigBuilder(hooks).getCodecContext().setDebug();
    ThreadConfig.setConfig(options.getConfig());

    JSession session = Jack.getSession();
    session.getLookup().getOrCreatePackage("java/lang");
  }

  @AfterClass
  public static void tearDown() {
    hooks.runHooks();
    ThreadConfig.unsetConfig();
  }

  @Test
  public void unaryPrefixNotGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;

    try {
      catched = false;
      new JPrefixNotOperation(SourceInfo.UNKNOWN, new JLongLiteral(SourceInfo.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // !long is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixNotOperation(
          SourceInfo.UNKNOWN, new JShortLiteral(SourceInfo.UNKNOWN, (short) 1)).getType();
    } catch (AssertionError e) {
      // !short is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JPrefixNotOperation(
        SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JPrefixNotOperation(
        SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN)).getType());
  }

  @Test
  public void unaryPrefixBitNotGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;

    try {
      catched = false;
      new JPrefixBitNotOperation(SourceInfo.UNKNOWN, new JDoubleLiteral(SourceInfo.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // ~double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixBitNotOperation(SourceInfo.UNKNOWN, new JFloatLiteral(SourceInfo.UNKNOWN, 1))
          .getType();
    } catch (AssertionError e) {
      // ~float is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JPrefixBitNotOperation(
          SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType();
    } catch (AssertionError e) {
      // ~boolean is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JPrefixBitNotOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN))
          .getType();
    } catch (AssertionError e) {
      // ~java.lang.Double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceInfo.UNKNOWN, new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceInfo.UNKNOWN, new JShortLiteral(SourceInfo.UNKNOWN, (short) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JPrefixBitNotOperation(
        SourceInfo.UNKNOWN, new JLongLiteral(SourceInfo.UNKNOWN, 1l)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CHAR);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPrefixBitNotOperation(
        SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN)).getType());
  }

  @Test
  public void unaryPostfixIncGetType() throws Exception {
    boolean catched;

    try {
      catched = false;
      new JPostfixDecOperation(
          SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType();
    } catch (AssertionError e) {
      // boolean-- is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JPostfixDecOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN))
          .getType();
    } catch (AssertionError e) {
      // Boolean-- is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceInfo.UNKNOWN, new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceInfo.UNKNOWN, new JShortLiteral(SourceInfo.UNKNOWN, (short) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.DOUBLE.getType(), new JPostfixDecOperation(
        SourceInfo.UNKNOWN, new JDoubleLiteral(SourceInfo.UNKNOWN, 1.0)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JPostfixDecOperation(
        SourceInfo.UNKNOWN, new JLongLiteral(SourceInfo.UNKNOWN, 1l)).getType());

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JPostfixDecOperation(
        SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN)).getType());
  }

  @Test
  public void shiftGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;

    try {
      catched = false;
      new JShlOperation(SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean << int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      new JShlOperation(SourceInfo.UNKNOWN, new JIntLiteral(SourceInfo.UNKNOWN, 1),
          new JFloatLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // int << float is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JShlOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Double << int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JShlOperation(SourceInfo.UNKNOWN, new JIntLiteral(SourceInfo.UNKNOWN, 1),
          l.makeRef(SourceInfo.UNKNOWN)).getType();
    } catch (AssertionError e) {
      // int << Double is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceInfo.UNKNOWN,
        new JIntLiteral(SourceInfo.UNKNOWN, 1), new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JShlOperation(SourceInfo.UNKNOWN,
        new JLongLiteral(SourceInfo.UNKNOWN, 1), new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceInfo.UNKNOWN,
        new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1),
        new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1)).getType());

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceInfo.UNKNOWN,
        new JCharLiteral(SourceInfo.UNKNOWN, (char) 1),
        new JShortLiteral(SourceInfo.UNKNOWN, (short) 1)).getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceInfo.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JShlOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN), l1.makeRef(SourceInfo.UNKNOWN))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_LONG);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceInfo.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.LONG.getType(), new JShlOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN), l1.makeRef(SourceInfo.UNKNOWN))
          .getType());
    }
  }

  @Test
  public void relationalGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;
    try {
      catched = false;
      new JLteOperation(SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean <= int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JLteOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
          new JFloatLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // ref object <= int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_DOUBLE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN), new JIntLiteral(SourceInfo.UNKNOWN, 1))
          .getType());
    }

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceInfo.UNKNOWN,
        new JIntLiteral(SourceInfo.UNKNOWN, 1), new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceInfo.UNKNOWN,
        new JLongLiteral(SourceInfo.UNKNOWN, 1), new JFloatLiteral(SourceInfo.UNKNOWN, 1))
        .getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BYTE);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JLocal l1 = new JLocal(SourceInfo.UNKNOWN, "test2", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JLteOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN), l1.makeRef(SourceInfo.UNKNOWN))
          .getType());
    }
  }

  @Test
  public void logicalAndBitwiseGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;
    try {
      catched = false;
      new JBitAndOperation(SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean & int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JBitAndOperation(SourceInfo.UNKNOWN,
        new JBooleanLiteral(SourceInfo.UNKNOWN, true),
        new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType());

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JBitAndOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN),l.makeRef(SourceInfo.UNKNOWN))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_INTEGER);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JBitAndOperation(SourceInfo.UNKNOWN,
          l.makeRef(SourceInfo.UNKNOWN), l.makeRef(SourceInfo.UNKNOWN))
          .getType());
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_INTEGER);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      JType t1 = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l1 = new JLocal(SourceInfo.UNKNOWN, "test", t1, JModifier.DEFAULT, null);
      try {
        catched = false;
        new JBitAndOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
            l1.makeRef(SourceInfo.UNKNOWN)).getType();
      } catch (AssertionError e) {
        // Integer & Object is not valid
        catched = true;
      }
      Assert.assertTrue(catched);
    }

    {
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_FLOAT);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      try {
        catched = false;
        new JBitAndOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
            new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
      } catch (AssertionError e) {
        // Float & int is not valid
        catched = true;
      }
      Assert.assertTrue(catched);
    }

    try {
      catched = false;
      new JBitAndOperation(SourceInfo.UNKNOWN, new JDoubleLiteral(SourceInfo.UNKNOWN, 1),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // double & int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);
  }

  @Test
  public void equalityGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceInfo.UNKNOWN,
        new JBooleanLiteral(SourceInfo.UNKNOWN, true),
        new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType());

    try {
      catched = false;
      new JEqOperation(SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean == int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceInfo.UNKNOWN,
        new JIntLiteral(SourceInfo.UNKNOWN, 1), new JIntLiteral(SourceInfo.UNKNOWN, 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceInfo.UNKNOWN,
        new JFloatLiteral(SourceInfo.UNKNOWN, 1), new JIntLiteral(SourceInfo.UNKNOWN, 1))
        .getType());

    try {
      catched = false;
      JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
      JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
      new JEqOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Object == int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JEqOperation(SourceInfo.UNKNOWN,
        l.makeRef(SourceInfo.UNKNOWN), l.makeRef(SourceInfo.UNKNOWN)).getType());
  }

  @Test
  public void conditionalOperationGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;

    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JAndOperation(SourceInfo.UNKNOWN,
        new JBooleanLiteral(SourceInfo.UNKNOWN, true),
        new JBooleanLiteral(SourceInfo.UNKNOWN, true)).getType());

    try {
      catched = false;
      new JAndOperation(SourceInfo.UNKNOWN, new JBooleanLiteral(SourceInfo.UNKNOWN, true),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // boolean && int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    JType t = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", t, JModifier.DEFAULT, null);
    Assert.assertEquals(JPrimitiveTypeEnum.BOOLEAN.getType(), new JAndOperation(SourceInfo.UNKNOWN,
        l.makeRef(SourceInfo.UNKNOWN), l.makeRef(SourceInfo.UNKNOWN)).getType());
  }

  @Test
  public void arithmeticGetType() throws Exception {
    Assume.assumeTrue(TestTools.areAssertionsEnabled());

    boolean catched;
    JType objectType = Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
    JLocal l = new JLocal(SourceInfo.UNKNOWN, "test", objectType, JModifier.DEFAULT, null);

    try {
      catched = false;
      new JAddOperation(SourceInfo.UNKNOWN, l.makeRef(SourceInfo.UNKNOWN),
          new JIntLiteral(SourceInfo.UNKNOWN, 1)).getType();
    } catch (AssertionError e) {
      // Object add int is not valid
      catched = true;
    }
    Assert.assertTrue(catched);

    Assert.assertEquals(JPrimitiveTypeEnum.INT.getType(), new JAddOperation(SourceInfo.UNKNOWN,
        new JByteLiteral(SourceInfo.UNKNOWN, (byte) 1), new JIntLiteral(SourceInfo.UNKNOWN, 1))
        .getType());

    Assert.assertEquals(JPrimitiveTypeEnum.DOUBLE.getType(), new JAddOperation(SourceInfo.UNKNOWN,
        new JDoubleLiteral(SourceInfo.UNKNOWN, 1), new JFloatLiteral(SourceInfo.UNKNOWN, 1))
        .getType());
  }

}
