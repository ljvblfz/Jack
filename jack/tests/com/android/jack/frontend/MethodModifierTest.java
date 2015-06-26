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
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MethodModifierTest {

  private JSession session;
  private static final String classBinaryName = "com/android/jack/modifier/jack/MethodModifier";
  private static final String classSignature = "L" + classBinaryName + ";";

  @Before
  public void setUp() throws Exception {
    Options jackArgs = TestTools.buildCommandLineArgs(
        TestTools.getJackTestFromBinaryName(classBinaryName));
    jackArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");

    session = TestTools.buildSession(jackArgs);
    Assert.assertNotNull(session);
  }

  @Test
  public void methodPublicModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodPublic()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isPublic());
  }

  @Test
  public void methodProtectedModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodProtected()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isProtected());
  }

  @Test
  public void methodPrivateModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodPrivate()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isPrivate());
  }

  @Test
  public void methodStaticModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodStatic()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isStatic());
  }

  @Test
  public void methodFinalModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodFinal()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isFinal());
  }

  @Test
  public void methodPublicFinalModifier() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodPublicFinal()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isPublic() && method.isFinal());
  }

  @Test
  public void methodSynchronized() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodSynchronized()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isSynchronized());
  }

  @Test
  public void methodNative() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodNative()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isNative());
  }

  @Test
  @Ignore
  public void methodVarags() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodVarags([I)V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isVarags());
  }

  @Test
  public void constructorPrivate() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "<init>()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isPrivate());
  }

  @Test
  public void constructorPublic() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "<init>(I)V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isPublic());
  }

  @Test
  public void methodAbstract() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodAbstract()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isAbstract());
  }

  @Test
  public void clinit() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = type.getMethod("<clinit>", JPrimitiveTypeEnum.VOID.getType());
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isStatic());
  }

  @Test
  public void methodStrictfp() throws Exception {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) session.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod method = TestTools.getMethod(type, "methodStrictfp()V");
    Assert.assertNotNull(method);

    Assert.assertTrue(method.isStrictfp());
  }
}
