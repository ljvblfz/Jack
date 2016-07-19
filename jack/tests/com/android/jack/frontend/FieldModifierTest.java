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
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JSession;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FieldModifierTest {

  private JSession session;

  private final static String FIELD_MODIFIER_BINARY_NAME =
      "com/android/jack/modifier/jack/FieldModifier";
  private final static String FIELD_MODIFIER_SIGNATURE = "L" + FIELD_MODIFIER_BINARY_NAME + ";";
  private final static String FIELD_ENUM_BINARY_NAME =
      "com/android/jack/modifier/jack/FieldEnumModifier";

  @Before
  public void setUp() throws Exception {
    Options jackArgs =
        TestTools.buildCommandLineArgs(TestTools
            .getJackTestFromBinaryName(FIELD_MODIFIER_BINARY_NAME));
    jackArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");

    session = TestTools.buildSession(jackArgs);
    Assert.assertNotNull(session);
  }

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void fieldPublicModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldPublic");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isPublic());
  }

  @Test
  public void fieldProtectedModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldProtected");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isProtected());
  }

  @Test
  public void fieldPrivateModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldPrivate");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isPrivate());
  }

  @Test
  public void fieldStaticModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldStatic");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isStatic());
  }

  @Test
  public void fieldVolatileModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldVolatile");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isVolatile());
  }

  @Test
  public void fieldFinalModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldFinal");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isFinal());
  }

  @Test
  public void fieldTransientModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldTransient");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isTransient());
  }

  @Test
  public void fieldPublicFinalModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldPublicFinal");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isPublic() && field.isFinal());
  }

  @Test
  public void fieldMultipleFinalModifier() throws Exception {
    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(FIELD_MODIFIER_SIGNATURE);
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "fieldProtectedStaticVolatileTransient");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isProtected() && field.isStatic() && field.isVolatile()
        && field.isTransient());
  }

  @Test
  public void fieldEnumModifier() throws Exception {
    ThreadConfig.unsetConfig(); // clean config from setUp, which is unused here

    Options args = TestTools.buildCommandLineArgs(
        TestTools.getJackTestFromBinaryName(FIELD_ENUM_BINARY_NAME));
    args.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");

    JSession session = TestTools.buildSession(args);
    Assert.assertNotNull(session);

    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(
            "L" + FIELD_ENUM_BINARY_NAME + "$Select;");
    Assert.assertNotNull(type);

    JField field = getFieldFromName(type, "ONE");
    Assert.assertNotNull(field);

    Assert.assertTrue(field.isFinal() && field.isStatic() && field.isEnum());
  }

  private JField getFieldFromName(JDefinedClassOrInterface type, String fieldName) {
    for (JField field : type.getFields()) {
      if (field.getName().equals(fieldName)) {
          return (field);
      }
    }
    return null;
  }

}
