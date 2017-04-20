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
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Lambda with obfuscation to check that MethodId is well managed
 */
public class Tests {

  private static final String STATIC_METHOD_RESPONSE = "StaticMethodResponse";

  @Test
  public void testNonCapturingLambda() throws Exception {
    Callable<String> r1 = () -> "Hello World";
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaImplementsInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertCallableBehavior(r1, "Hello World");

    List<Callable<String>> callables = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      callables.add(() -> "Hello World");
    }
    assertMultipleDefinitionCharacteristics(r1, callables.get(0));
    assertMultipleInstanceCharacteristics(callables.get(0), callables.get(1));
  }

  interface Condition<T> {
    boolean check(T arg);
  }

  @Test
  public void testInstanceMethodReferenceLambda() throws Exception {
    Condition<String> c = String::isEmpty;
    Class<?> lambdaClass = c.getClass();
    assertGeneralLambdaClassCharacteristics(c);
    assertLambdaImplementsInterfaces(c, Condition.class);
    assertLambdaMethodCharacteristics(c, Condition.class);

    // Check the behavior of the lambda's method.
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
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaImplementsInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);

    assertCallableBehavior(r1, STATIC_METHOD_RESPONSE);

    List<Callable<String>> callables = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      callables.add(Tests::staticMethod);
    }
    assertMultipleInstanceCharacteristics(callables.get(0), callables.get(1));
  }

  @Test
  public void testObjectMethodReferenceLambda() throws Exception {
    String msg = "Hello";
    StringBuilder o = new StringBuilder(msg);
    Callable<String> r1 = o::toString;
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaImplementsInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);

    assertCallableBehavior(r1, msg);

    List<Callable<String>> callables = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      callables.add(o::toString);
    }
    assertMultipleDefinitionCharacteristics(r1, callables.get(0));
    assertMultipleInstanceCharacteristics(callables.get(0), callables.get(1));
  }

  @Test
  public void testArgumentCapturingLambda() throws Exception {
    checkArgumentCapturingLambda("Argument");
  }

  private void checkArgumentCapturingLambda(String msg) throws Exception {
    Callable<String> r1 = () -> msg;
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaImplementsInterfaces(r1, Callable.class);
    assertLambdaMethodCharacteristics(r1, Callable.class);

    assertCallableBehavior(r1, msg);

    List<Callable<String>> callables = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      callables.add(() -> msg);
    }
    assertMultipleDefinitionCharacteristics(r1, callables.get(0));
    assertMultipleInstanceCharacteristics(callables.get(0), callables.get(1));
  }

  @Test
  public void testMultipleInterfaceLambda() throws Exception {
    Callable<String> r1 = (Callable<String> & MarkerInterface) () -> "MultipleInterfaces";
    Assert.assertTrue(r1 instanceof MarkerInterface);
    assertGeneralLambdaClassCharacteristics(r1);
    assertLambdaMethodCharacteristics(r1, Callable.class);
    assertLambdaImplementsInterfaces(r1, Callable.class, MarkerInterface.class);

    assertCallableBehavior(r1, "MultipleInterfaces");

    List<Callable<String>> callables = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      Callable<String> callable = (Callable<String> & MarkerInterface) () -> "MultipleInterfaces";
      assertLambdaImplementsInterfaces(callable, Callable.class, MarkerInterface.class);
      callables.add(callable);
    }
    assertLambdaImplementsInterfaces(r1, Callable.class, MarkerInterface.class);
    assertMultipleDefinitionCharacteristics(r1, callables.get(0));
    assertMultipleInstanceCharacteristics(callables.get(0), callables.get(1));
  }

  private static <T> void assertLambdaImplementsInterfaces(T r1, Class<?>... expectedInterfaces)
          throws Exception {
    Class<?> lambdaClass = r1.getClass();

    // Check directly implemented interfaces. Ordering is well-defined.
    Class<?>[] actualInterfaces = lambdaClass.getInterfaces();
    Assert.assertEquals(expectedInterfaces.length, actualInterfaces.length);
    List<Class<?>> actual = Arrays.asList(actualInterfaces);
    List<Class<?>> expected = Arrays.asList(expectedInterfaces);
    Assert.assertEquals(expected, actual);

    // Confirm that the only method declared on the lambda's class are those defined by
    // interfaces it implements. i.e. there's no additional public contract.
    Set<Method> declaredMethods = new HashSet<>();
    addNonStaticPublicMethods(lambdaClass, declaredMethods);
    Set<Method> expectedMethods = new HashSet<>();
    for (Class<?> interfaceClass : expectedInterfaces) {
      // Obtain methods declared by super-interfaces too.
      while (interfaceClass != null) {
        addNonStaticPublicMethods(interfaceClass, expectedMethods);
        interfaceClass = interfaceClass.getSuperclass();
      }
    }
    Assert.assertEquals(expectedMethods.size(), declaredMethods.size());

    // Check the method signatures are compatible.
    for (Method expectedMethod : expectedMethods) {
      Method actualMethod =
          lambdaClass.getMethod(expectedMethod.getName(), expectedMethod.getParameterTypes());
      Assert.assertEquals(expectedMethod.getReturnType(), actualMethod.getReturnType());
    }
  }

  private static void addNonStaticPublicMethods(Class<?> clazz, Set<Method> methodSet) {
    for (Method interfaceMethod : clazz.getDeclaredMethods()) {
      int modifiers = interfaceMethod.getModifiers();
      if ((!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))) {
        methodSet.add(interfaceMethod);
      }
    }
  }


  /**
   * Asserts that necessary conditions hold when there are two lambdas with separate but identical
   * definitions.
   */
  private static void assertMultipleDefinitionCharacteristics(
          Callable<String> r1, Callable<String> r2) throws Exception {

    // Sanity check that the lambdas do the same thing.
    Assert.assertEquals(r1.call(), r2.call());

    // Unclear if any of this is *guaranteed* to be true.

    // Check the objects are not the same and do not equal. This could influence collection
    // behavior.
    Assert.assertNotSame(r1, r2);
    Assert.assertTrue(!r1.equals(r2));

    // Two lambdas from different definitions can share the same class or may not.
    // See JLS 15.27.4.
  }

  /**
   * Asserts that necessary conditions hold when there are two lambdas created from the same
   * definition.
   */
  private static void assertMultipleInstanceCharacteristics(
          Callable<String> r1, Callable<String> r2) throws Exception {

    // Sanity check that the lambdas do the same thing.
    Assert.assertEquals(r1.call(), r2.call());

    // There doesn't appear to be anything else that is safe to assert here. Two lambdas
    // created from the same definition can be the same, as can their class, but they can also
    // be different. See JLS 15.27.4.
  }

  private static void assertGeneralLambdaClassCharacteristics(Object r1) throws Exception {
    Class<?> lambdaClass = r1.getClass();

    // Lambda objects have classes that have names.
    Assert.assertNotNull(lambdaClass.getName());
    Assert.assertNotNull(lambdaClass.getSimpleName());
    Assert.assertNotNull(lambdaClass.getCanonicalName());

    // Lambda classes are "synthetic classes" that are not arrays.
    Assert.assertFalse(lambdaClass.isAnnotation());
    Assert.assertFalse(lambdaClass.isInterface());
    Assert.assertFalse(lambdaClass.isArray());
    Assert.assertFalse(lambdaClass.isEnum());
    Assert.assertFalse(lambdaClass.isPrimitive());
    Assert.assertTrue(lambdaClass.isSynthetic());
    Assert.assertNull(lambdaClass.getComponentType());

    // Expected modifiers
    int classModifiers = lambdaClass.getModifiers();
    Assert.assertTrue(Modifier.isFinal(classModifiers));

    // Unexpected modifiers
    Assert.assertFalse(Modifier.isPrivate(classModifiers));
    Assert.assertFalse(Modifier.isPublic(classModifiers));
    Assert.assertFalse(Modifier.isProtected(classModifiers));
    Assert.assertFalse(Modifier.isStatic(classModifiers));
    Assert.assertFalse(Modifier.isSynchronized(classModifiers));
    Assert.assertFalse(Modifier.isVolatile(classModifiers));
    Assert.assertFalse(Modifier.isTransient(classModifiers));
    Assert.assertFalse(Modifier.isNative(classModifiers));
    Assert.assertFalse(Modifier.isInterface(classModifiers));
    Assert.assertFalse(Modifier.isAbstract(classModifiers));
    Assert.assertFalse(Modifier.isStrict(classModifiers));

    // Check the classloader, inheritance hierarchy and package.
    Assert.assertSame(Tests.class.getClassLoader(), lambdaClass.getClassLoader());
    Assert.assertSame(Object.class, lambdaClass.getSuperclass());
    Assert.assertSame(Object.class, lambdaClass.getGenericSuperclass());
    Assert.assertEquals(Tests.class.getPackage(), lambdaClass.getPackage());

    // Check the implementation of the non-final public methods that all Objects possess.
    Assert.assertNotNull(r1.toString());
    Assert.assertTrue(r1.equals(r1));
    Assert.assertEquals(System.identityHashCode(r1), r1.hashCode());
  }

  private static <T> void assertLambdaMethodCharacteristics(T r1, Class<?> samInterfaceClass)
          throws Exception {
    // Find the single abstract method on the interface.
    Method singleAbstractMethod = null;
    for (Method method : samInterfaceClass.getDeclaredMethods()) {
      if (Modifier.isAbstract(method.getModifiers())) {
        singleAbstractMethod = method;
        break;
      }
    }
    Assert.assertNotNull(singleAbstractMethod);

    // Confirm the lambda implements the method as expected.
    Method implementationMethod = r1.getClass().getMethod(singleAbstractMethod.getName(),
        singleAbstractMethod.getParameterTypes());
    Assert.assertSame(singleAbstractMethod.getReturnType(), implementationMethod.getReturnType());
    Assert.assertSame(r1.getClass(), implementationMethod.getDeclaringClass());
    Assert.assertFalse(implementationMethod.isSynthetic());
    Assert.assertFalse(implementationMethod.isBridge());
  }

  private static String staticMethod() {
    return STATIC_METHOD_RESPONSE;
  }

  private interface MarkerInterface {
  }

  private static <T> void assertCallableBehavior(Callable<T> r1, T expectedResult)
          throws Exception {
    Assert.assertEquals(expectedResult, r1.call());

    Method implCallMethod = r1.getClass().getDeclaredMethod("call");
    Assert.assertEquals(expectedResult, implCallMethod.invoke(r1));

    Method interfaceCallMethod = Callable.class.getDeclaredMethod("call");
    Assert.assertEquals(expectedResult, interfaceCallMethod.invoke(r1));
  }
}
