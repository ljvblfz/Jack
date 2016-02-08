/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.java8.lambda.test035.jack;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Lambda with obfuscation to check that MethodId is well managed
 */
public class Tests {

  private static final String MSG = "Hello World";

  private static String staticMethod() {
    return MSG;
  }

  private interface MarkerInterface {
  }

  interface Condition<T> {
    boolean check(T arg);
  }

  @Test
  public void nonCapturingLambda() throws Exception {
    Callable<String> r1 = () -> MSG;
    Assert.assertNotNull(r1.getClass().getName());
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertNonSerializableLambdaCharacteristics(r1);

    assertCallableBehavior(r1, MSG);

    Callable r2 = () -> MSG;
    Assert.assertNotNull(r2.getClass().getName());
    assertMultipleInstanceCharacteristics(r1, r2);
  }

  @Test
  public void testInstanceMethodReferenceLambda() throws Exception {
    Condition<String> c = String::isEmpty;
    Class<?> lambdaClass = c.getClass();
    Assert.assertNotNull(lambdaClass.getName());
    assertGeneralLambdaClassCharacteristics(c);
    assertLambdaInterfaces(c, Condition.class);
    assertLambdaMethodCharacteristics(c, Condition.class);
    assertNonSerializableLambdaCharacteristics(c);

    Assert.assertTrue(c.check(""));
    Assert.assertFalse(c.check("notEmpty"));

    Method implCallMethod =
        lambdaClass.getMethod("check", Object.class /* type erasure => not String.class */);
    Assert.assertTrue((Boolean) implCallMethod.invoke(c, ""));
    Assert.assertFalse((Boolean) implCallMethod.invoke(c, "notEmpty"));

    Method interfaceCallMethod = Condition.class.getDeclaredMethod("check",
        Object.class /* type erasure => not String.class */);
    Assert.assertTrue((Boolean) interfaceCallMethod.invoke(c, ""));
    Assert.assertFalse((Boolean) interfaceCallMethod.invoke(c, "notEmpty"));
  }

  @Test
  public void testStaticMethodReferenceLambda() throws Exception {
    Callable<String> r1 = Tests::staticMethod;
    Assert.assertNotNull(r1.getClass().getName());
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertNonSerializableLambdaCharacteristics(r1);

    assertCallableBehavior(r1, MSG);

    Callable<String> r2 = Tests::staticMethod;
    Assert.assertNotNull(r2.getClass().getName());
    assertMultipleInstanceCharacteristics(r1, r2);
  }

  @Test
  public void testObjectMethodReferenceLambda() throws Exception {
    StringBuilder o = new StringBuilder(MSG);
    Callable<String> r1 = o::toString;
    Assert.assertNotNull(r1.getClass().getName());
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertNonSerializableLambdaCharacteristics(r1);

    assertCallableBehavior(r1, MSG);

    Callable<String> r2 = o::toString;
    Assert.assertNotNull(r2.getClass().getName());
    assertMultipleInstanceCharacteristics(r1, r2);
  }

  @Test
  public void testArgumentCapturingLambda() throws Exception {
    String msg = MSG;
    Callable<String> r1 = () -> msg;
    Assert.assertNotNull(r1.getClass().getName());
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertNonSerializableLambdaCharacteristics(r1);

    assertCallableBehavior(r1, MSG);

    Callable<String> r2 = () -> msg;
    Assert.assertNotNull(r2.getClass().getName());
    assertMultipleInstanceCharacteristics(r1, r2);
  }

  @Test
  public void testMultipleInterfaceLambda() throws Exception {
    Callable<String> r1 = (Callable<String> & MarkerInterface) () -> MSG;
    Assert.assertTrue(r1 instanceof MarkerInterface);
    Assert.assertNotNull(r1.getClass().getName());
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertLambdaInterfaces(r1, Callable.class, MarkerInterface.class);
    assertNonSerializableLambdaCharacteristics(r1);

    assertCallableBehavior(r1, MSG);
  }



  private static void assertNonSerializableLambdaCharacteristics(Object r1) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
      os.writeObject(r1);
      os.flush();
      Assert.fail();
    } catch (ObjectStreamException expected) {
    }
  }

  private static void assertMultipleInstanceCharacteristics(Object r1, Object r2) throws Exception {
    Assert.assertNotSame(r1, r2);
    Assert.assertTrue(!r1.equals(r2));

    Class<?> lambda1Class = r1.getClass();
    Class<?> lambda2Class = r2.getClass();
    Assert.assertNotSame(lambda1Class, lambda2Class);
  }

  private static <T> void assertCallableBehavior(Callable<T> r1, T expectedResult)
      throws Exception {
    Assert.assertEquals(expectedResult, r1.call());
    Method implCallMethod = r1.getClass().getDeclaredMethod("call");
    Assert.assertEquals(expectedResult, implCallMethod.invoke(r1));

    Method interfaceCallMethod = Callable.class.getDeclaredMethod("call");
    Assert.assertEquals(expectedResult, interfaceCallMethod.invoke(r1));
  }

  private static void assertGeneralLambdaClassCharacteristics(Object r1) throws Exception {
    Class<?> lambdaClass = r1.getClass();

    Assert.assertFalse(lambdaClass.isAnnotation());
    Assert.assertFalse(lambdaClass.isAnonymousClass());
    Assert.assertFalse(lambdaClass.isInterface());
    Assert.assertFalse(lambdaClass.isLocalClass());
    Assert.assertNull(lambdaClass.getEnclosingMethod());
    Assert.assertNull(lambdaClass.getEnclosingConstructor());
    Assert.assertNotNull(lambdaClass.getDeclaringClass());
    Assert.assertNotNull(lambdaClass.getEnclosingClass());
    Assert.assertTrue(lambdaClass.isMemberClass());
    Assert.assertEquals(1, lambdaClass.getConstructors().length);

    Assert.assertNotNull(lambdaClass.getSimpleName());
    Assert.assertNotNull(lambdaClass.getCanonicalName());
    Assert.assertEquals(0, lambdaClass.getClasses().length);
    Assert.assertFalse(lambdaClass.isArray());
    Assert.assertFalse(lambdaClass.isEnum());
    Assert.assertFalse(lambdaClass.isPrimitive());
    Assert.assertTrue(lambdaClass.isSynthetic());
    Assert.assertNull(lambdaClass.getComponentType());
    Assert.assertTrue((Modifier.isFinal(lambdaClass.getModifiers())));
    // Unexpected modifiers
    int unexpectedModifiers = Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED
        | Modifier.STATIC | Modifier.SYNCHRONIZED | Modifier.VOLATILE | Modifier.TRANSIENT
        | Modifier.NATIVE | Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.STRICT;
    Assert.assertTrue((unexpectedModifiers & lambdaClass.getModifiers()) == 0);
    Assert.assertNull(lambdaClass.getSigners());

    Assert.assertSame(Tests.class.getClassLoader(), lambdaClass.getClassLoader());
    Assert.assertEquals(0, lambdaClass.getTypeParameters().length);

    Assert.assertSame(Object.class, lambdaClass.getSuperclass());
    Assert.assertSame(Object.class, lambdaClass.getGenericSuperclass());
    Assert.assertEquals(Tests.class.getPackage(), lambdaClass.getPackage());

    Assert.assertNotNull(r1.toString());
    Assert.assertTrue(r1.equals(r1));
    Assert.assertEquals(System.identityHashCode(r1), r1.hashCode());

  }

  private static <T> void assertLambdaMethodCharacteristics(T r1, Class<?> samInterfaceClass)
      throws Exception {
    Method singleAbstractMethod = null;
    for (Method method : samInterfaceClass.getDeclaredMethods()) {
      if (Modifier.isAbstract(method.getModifiers())) {
        singleAbstractMethod = method;
        break;
      }
    }
    Assert.assertNotNull(singleAbstractMethod);

    Method implementationMethod = r1.getClass().getMethod(singleAbstractMethod.getName(),
        singleAbstractMethod.getParameterTypes());
    Assert.assertSame(singleAbstractMethod.getReturnType(), implementationMethod.getReturnType());
    Assert.assertSame(r1.getClass(), implementationMethod.getDeclaringClass());
    Assert.assertFalse(implementationMethod.isSynthetic());
    Assert.assertFalse(implementationMethod.isBridge());
  }

  private static <T> void assertLambdaInterfaces(T r1, Class<?>... interfaceClasses)
      throws Exception {
    Class<?> lambdaClass = r1.getClass();
    Assert.assertEquals(interfaceClasses.length, lambdaClass.getInterfaces().length);
    Set<Class<?>> actual = new HashSet<>(Arrays.asList(lambdaClass.getInterfaces()));
    Set<Class<?>> expected = new HashSet<>(Arrays.asList(interfaceClasses));
    Assert.assertEquals(expected, actual);

    Set<Method> declaredMethods = new HashSet<>();
    addNonStaticPublicMethods(declaredMethods, lambdaClass);
    Set<Method> expectedMethods = new HashSet<>();
    for (Class<?> interfaceClass : interfaceClasses) {
      while (interfaceClass != null) {
        addNonStaticPublicMethods(expectedMethods, interfaceClass);
        interfaceClass = interfaceClass.getSuperclass();
      }
    }
    Assert.assertEquals(expectedMethods.size(), declaredMethods.size());
    for (Method expectedMethod : expectedMethods) {
      Method actualMethod =
          lambdaClass.getMethod(expectedMethod.getName(), expectedMethod.getParameterTypes());
      Assert.assertEquals(expectedMethod.getReturnType(), actualMethod.getReturnType());
    }
  }

  private static void addNonStaticPublicMethods(Set<Method> methodSet, Class<?> clazz) {
    for (Method interfaceMethod : clazz.getDeclaredMethods()) {
      int modifiers = interfaceMethod.getModifiers();
      if ((!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))) {
        methodSet.add(interfaceMethod);
      }
    }
  }
}
