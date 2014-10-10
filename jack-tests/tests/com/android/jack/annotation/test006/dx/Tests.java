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

package com.android.jack.annotation.test006.dx;



import com.android.jack.annotation.test006.jack.ReflectAnnotationsTest;
import com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Tests {

  @Test
  public void test001() {
    ReflectAnnotationsTest test = new ReflectAnnotationsTest();
    Class<?> testClass = test.getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getName());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getCanonicalName());
    Assert.assertEquals("ReflectAnnotationsTest", testClass.getSimpleName());
    Assert.assertEquals("class java.lang.Object", testClass.getSuperclass().toString());
    Assert.assertEquals("class java.lang.Object", testClass.getGenericSuperclass().toString());
    Assert.assertNull(testClass.getDeclaringClass());
    Assert.assertNull(testClass.getEnclosingClass());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 1);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    String declaredClasses = stringifyTypeArray(testClass.getDeclaredClasses());
    boolean testDeclaredClasses = declaredClasses.contains("[2]");
    testDeclaredClasses = testDeclaredClasses && declaredClasses.contains("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClass");
    testDeclaredClasses = testDeclaredClasses && declaredClasses.contains("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClassStatic");
    Assert.assertTrue(testDeclaredClasses);
    String memberClasses = stringifyTypeArray(testClass.getClasses());
    boolean memberClassesTest = memberClasses.contains("[2]");
    memberClassesTest = memberClassesTest && memberClasses.contains("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClass");
    memberClassesTest = memberClassesTest && memberClasses.contains("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClassStatic");
    Assert.assertTrue(memberClassesTest);
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertFalse(testClass.isLocalClass());
    Assert.assertFalse(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));


    testClass = test.getLocal(3).getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$1C", testClass.getName());
    Assert.assertNull(testClass.getCanonicalName());
    Assert.assertEquals("C", testClass.getSimpleName());
    Assert.assertEquals("class java.lang.Object", testClass.getSuperclass().toString());
    Assert.assertEquals("class java.lang.Object", testClass.getGenericSuperclass().toString());
    Assert.assertNull(testClass.getDeclaringClass());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getEnclosingClass().toString());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertEquals("public java.lang.Object com.android.jack.annotation.test006.jack.ReflectAnnotationsTest.getLocal(int)", testClass.getEnclosingMethod().toString());
    Assert.assertEquals(testClass.getModifiers(), 0);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getDeclaredClasses()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getClasses()));
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertTrue(testClass.isLocalClass());
    Assert.assertFalse(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));


    testClass = test.new InnerClass().getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClass", testClass.getName());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest.InnerClass", testClass.getCanonicalName());
    Assert.assertEquals("InnerClass", testClass.getSimpleName());
    Assert.assertEquals("class java.lang.Object", testClass.getSuperclass().toString());
    Assert.assertEquals("class java.lang.Object", testClass.getGenericSuperclass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getDeclaringClass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getEnclosingClass().toString());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 1);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getDeclaredClasses()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getClasses()));
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertFalse(testClass.isLocalClass());
    Assert.assertTrue(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));

    // --- static inner with inheritance ---
    testClass = new ReflectAnnotationsTest.InnerClassStatic().getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest$InnerClassStatic", testClass.getName());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest.InnerClassStatic", testClass.getCanonicalName());
    Assert.assertEquals("InnerClassStatic", testClass.getSimpleName());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.SuperClass", testClass.getSuperclass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.SuperClass", testClass.getGenericSuperclass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getDeclaringClass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest", testClass.getEnclosingClass().toString());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 9);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getDeclaredClasses()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getClasses()));
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertFalse(testClass.isLocalClass());
    Assert.assertTrue(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    String genInterfaces = stringifyTypeArray(testClass.getGenericInterfaces());
    boolean testGenInterfaces = genInterfaces.contains("[2]");
    testGenInterfaces = testGenInterfaces && genInterfaces.contains("com.android.jack.annotation.test006.jack.SuperInterface1");
    testGenInterfaces = testGenInterfaces && genInterfaces.contains("com.android.jack.annotation.test006.jack.SuperInterface2");
    Assert.assertTrue(testGenInterfaces);
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));
  }

  @Test
  public void test002() {
    Class<?> testClass = ReflectAnnotationsTest2.class;

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2", testClass.getName());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2", testClass.getCanonicalName());
    Assert.assertEquals("ReflectAnnotationsTest2", testClass.getSimpleName());
    Assert.assertEquals("class java.lang.Object", testClass.getSuperclass().toString());
    Assert.assertEquals("class java.lang.Object", testClass.getGenericSuperclass().toString());
    Assert.assertNull(testClass.getDeclaringClass());
    Assert.assertNull(testClass.getEnclosingClass());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 1);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    String declaredClasses = stringifyTypeArray(testClass.getDeclaredClasses());
    boolean testDeclared = declaredClasses.contains("[2]");
    testDeclared = testDeclared && declaredClasses.contains("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$Generic2");
    testDeclared = testDeclared && declaredClasses.contains("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$Generic1");
    Assert.assertTrue(testDeclared);
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getClasses()));
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertFalse(testClass.isLocalClass());
    Assert.assertFalse(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));


    ReflectAnnotationsTest2 test = new ReflectAnnotationsTest2();
    testClass = test.consInnerNamed.getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$1ConsInnerNamed", testClass.getName());
    Assert.assertNull(testClass.getCanonicalName());
    Assert.assertEquals("ConsInnerNamed", testClass.getSimpleName());
    Assert.assertEquals("class java.lang.Object", testClass.getSuperclass().toString());
    Assert.assertEquals("class java.lang.Object", testClass.getGenericSuperclass().toString());
    Assert.assertNull(testClass.getDeclaringClass());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2", testClass.getEnclosingClass().toString());
    Assert.assertEquals("public com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2()", testClass.getEnclosingConstructor().toString());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 0);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getDeclaredClasses()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getClasses()));
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertTrue(testClass.isLocalClass());
    Assert.assertFalse(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getTypeParameters()));

    Object gen = test.getGeneric();
    testClass = gen.getClass();

    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$Generic2", testClass.getName());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2.Generic2", testClass.getCanonicalName());
    Assert.assertEquals("Generic2", testClass.getSimpleName());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$Generic1", testClass.getSuperclass().toString());
    Assert.assertEquals("com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2$Generic1<K, java.lang.String>", testClass.getGenericSuperclass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2", testClass.getDeclaringClass().toString());
    Assert.assertEquals("class com.android.jack.annotation.test006.jack.ReflectAnnotationsTest2", testClass.getEnclosingClass().toString());
    Assert.assertNull(testClass.getEnclosingConstructor());
    Assert.assertNull(testClass.getEnclosingMethod());
    Assert.assertEquals(testClass.getModifiers(), 2);
    Assert.assertEquals("package com.android.jack.annotation.test006.jack", testClass.getPackage().toString());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getDeclaredClasses()));
    String memberClasses = stringifyTypeArray(testClass.getClasses());
    boolean testMemberClasses = memberClasses.contains("java.util.AbstractMap$SimpleEntry");
    testMemberClasses = testMemberClasses && memberClasses.contains("java.util.AbstractMap$SimpleImmutableEntry");
    Assert.assertTrue(testMemberClasses);
    Assert.assertFalse(testClass.isAnnotation());
    Assert.assertFalse(testClass.isAnonymousClass());
    Assert.assertFalse(testClass.isArray());
    Assert.assertFalse(testClass.isEnum());
    Assert.assertFalse(testClass.isInterface());
    Assert.assertFalse(testClass.isLocalClass());
    Assert.assertTrue(testClass.isMemberClass());
    Assert.assertFalse(testClass.isPrimitive());
    Assert.assertFalse(testClass.isSynthetic());
    Assert.assertEquals("[0]", stringifyTypeArray(testClass.getGenericInterfaces()));
    String typeParameters = stringifyTypeArray(testClass.getTypeParameters());
    boolean testTypeParameters = typeParameters.contains("[1]");
    testTypeParameters = testTypeParameters && typeParameters.contains("K");
    Assert.assertTrue(testTypeParameters);


    Method meth;
    try {
      meth = ReflectAnnotationsTest2.class.getMethod("foo", (Class[]) null);
      Assert.assertNull(getSignature(meth));

      Field field;
      field = ReflectAnnotationsTest2.class.getField("consInnerNamed");
      Assert.assertNull(getSignature(field));

      meth = ReflectAnnotationsTest2.class.getMethod("getGeneric", (Class[]) null);
      Assert.assertEquals("()Lcom/android/jack/annotation/test006/jack/ReflectAnnotationsTest2$Generic2<Ljava/lang/String;>;", getSignature(meth));
      Class<?>[] exceptions = meth.getExceptionTypes();
      Assert.assertTrue(exceptions.length == 2);
      List<Class<?>> exceptionsList = Arrays.asList(exceptions);
      Assert.assertTrue(exceptionsList.contains(OutOfMemoryError.class));
      Assert.assertTrue(exceptionsList.contains(AssertionError.class));

      field = ReflectAnnotationsTest2.class.getField("genField");
      Assert.assertEquals("Ljava/util/List<Ljava/lang/String;>;", getSignature(field));

    } catch (SecurityException e) {
      System.out.println(e.getMessage());
    } catch (NoSuchMethodException e) {
      System.out.println(e.getMessage());
    } catch (NoSuchFieldException e) {
      System.out.println(e.getMessage());
    }
  }

  private static String getSignature(Object obj) {
      Method method;
      try {
        Class<? extends Object> c = obj.getClass();
        method = c.getDeclaredMethod("getSignatureAttribute");
        method.setAccessible(true);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }

      try {
        return (String) method.invoke(obj);
      } catch (IllegalAccessException ex) {
          throw new RuntimeException(ex);
      } catch (InvocationTargetException ex) {
          throw new RuntimeException(ex);
      }
  }

  private static String stringifyTypeArray(Type[] types) {
      StringBuilder stb = new StringBuilder();
      boolean first = true;

      stb.append("[" + types.length + "]");

      for (Type t: types) {
          if (first) {
              stb.append(" ");
              first = false;
          } else {
              stb.append(", ");
          }
          stb.append(t.toString());
      }

      return stb.toString();
  }
}
