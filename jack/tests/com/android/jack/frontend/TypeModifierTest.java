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
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TypeModifierTest {
  private JSession session;
  private static final String OUTER_CLASS_BINARY_NAME = "com/android/jack/modifier/jack/TypeModifier";

  @Before
  public void setUp() throws Exception {
    Options jackArgs = TestTools.buildCommandLineArgs(
        TestTools.getJackTestFromBinaryName(OUTER_CLASS_BINARY_NAME));
    jackArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");
    session = TestTools.buildSession(jackArgs);
    Assert.assertNotNull(session);
  }

  private JDefinedClassOrInterface lookupInnerClass(String className) throws JTypeLookupException {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup()
        .getType("L" + OUTER_CLASS_BINARY_NAME + "$" + className + ";");
    Assert.assertNotNull(type);
    return type;
  }

  private static void assertInterfaceModifiers(JDefinedClassOrInterface type) {
    Assert.assertTrue(JModifier.isInterface(type.getModifier()));
    Assert.assertTrue(type.isAbstract());
  }

  private static void assertEnumModifiers(JDefinedClassOrInterface type) {
    Assert.assertTrue(JModifier.isEnum(type.getModifier()));
  }

  private static void assertAnnotationModifiers(JDefinedClassOrInterface type) {
    assertInterfaceModifiers(type);
    Assert.assertTrue(JModifier.isAnnotation(type.getModifier()));
  }

  @Test
  public void classPublicModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PublicClass");
    Assert.assertTrue(type.isPublic());
  }

  @Test
  public void interfacePublicModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PublicInterface");
    Assert.assertTrue(type.isPublic());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumPublicModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PublicEnum");
    Assert.assertTrue(type.isPublic());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationPublicModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PublicAnnotation");
    Assert.assertTrue(type.isPublic());
    assertAnnotationModifiers(type);
  }

  @Test
  public void classProtectedModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("ProtectedClass");
    Assert.assertTrue(type.isProtected());
  }

  @Test
  public void interfaceProtectedModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("ProtectedInterface");
    Assert.assertTrue(type.isProtected());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumProtectedModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("ProtectedEnum");
    Assert.assertTrue(type.isProtected());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationProtectedModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("ProtectedAnnotation");
    Assert.assertTrue(type.isProtected());
    assertAnnotationModifiers(type);
  }

  @Test
  public void classPrivateModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PrivateClass");
    Assert.assertTrue(type.isPrivate());
  }

  @Test
  public void interfacePrivateModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PrivateInterface");
    Assert.assertTrue(type.isPrivate());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumPrivateModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PrivateEnum");
    Assert.assertTrue(type.isPrivate());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationPrivateModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PrivateAnnotation");
    Assert.assertTrue(type.isPrivate());
    assertAnnotationModifiers(type);
  }

  @Test
  public void classPackageModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PackageClass");
    Assert.assertFalse(type.isPublic());
    Assert.assertFalse(type.isProtected());
    Assert.assertFalse(type.isPrivate());
  }

  @Test
  public void interfacePackageModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PackageInterface");
    Assert.assertFalse(type.isPublic());
    Assert.assertFalse(type.isProtected());
    Assert.assertFalse(type.isPrivate());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumPackageModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PackageEnum");
    Assert.assertFalse(type.isPublic());
    Assert.assertFalse(type.isProtected());
    Assert.assertFalse(type.isPrivate());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationPackageModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("PackageAnnotation");
    Assert.assertFalse(type.isPublic());
    Assert.assertFalse(type.isProtected());
    Assert.assertFalse(type.isPrivate());
    assertAnnotationModifiers(type);
  }

  @Test
  public void classStaticModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StaticClass");
    Assert.assertTrue(type.isStatic());
  }

  @Test
  public void interfaceStaticModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StaticInterface");
    Assert.assertTrue(type.isStatic());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumStaticModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StaticEnum");
    Assert.assertTrue(type.isStatic());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationStaticModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StaticAnnotation");
    Assert.assertTrue(type.isStatic());
    assertAnnotationModifiers(type);
  }

  @Test
  public void classAbstractModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("AbstractClass");
    Assert.assertTrue(type.isAbstract());
  }

  @Test
  public void classFinalModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("FinalClass");
    Assert.assertTrue(type.isFinal());
  }

  @Test
  public void classStrictfpModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StrictfpClass");
    Assert.assertTrue(type.isStrictfp());
  }

  @Test
  public void interfaceStrictfpModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StrictfpInterface");
    Assert.assertTrue(type.isStrictfp());
    assertInterfaceModifiers(type);
  }

  @Test
  public void enumStrictfpModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StrictfpEnum");
    Assert.assertTrue(type.isStrictfp());
    assertEnumModifiers(type);
  }

  @Test
  public void annotationStrictfpModifier() throws Exception {
    JDefinedClassOrInterface type = lookupInnerClass("StrictfpAnnotation");
    Assert.assertTrue(type.isStrictfp());
    assertAnnotationModifiers(type);
  }
}
