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

package com.android.jack.annotation.test001.dx;

import com.android.jack.annotation.test001.jack.Annotated1;
import com.android.jack.annotation.test001.jack.Annotated2;
import com.android.jack.annotation.test001.jack.Annotated3;
import com.android.jack.annotation.test001.jack.Annotated4;
import com.android.jack.annotation.test001.jack.Annotated5;
import com.android.jack.annotation.test001.jack.Annotation1;
import com.android.jack.annotation.test001.jack.Annotation10;
import com.android.jack.annotation.test001.jack.Annotation11;
import com.android.jack.annotation.test001.jack.Annotation12;
import com.android.jack.annotation.test001.jack.Annotation13;
import com.android.jack.annotation.test001.jack.Annotation14;
import com.android.jack.annotation.test001.jack.Annotation2;
import com.android.jack.annotation.test001.jack.Annotation3;
import com.android.jack.annotation.test001.jack.Annotation4;
import com.android.jack.annotation.test001.jack.Annotation5;
import com.android.jack.annotation.test001.jack.Annotation6;
import com.android.jack.annotation.test001.jack.Annotation7;
import com.android.jack.annotation.test001.jack.Annotation8;
import com.android.jack.annotation.test001.jack.Annotation9;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Tests {

  @Test
  public void test1() {
    Class<Annotation1> annotation1 = Annotation1.class;
    Assert.assertTrue(annotation1.isAnnotation());
    Field[] declaredFields = annotation1.getDeclaredFields();
    Assert.assertEquals(0, declaredFields.length);
    Method[] declaredMethods = annotation1.getDeclaredMethods();
    Assert.assertEquals(0, declaredMethods.length);
    Assert.assertNull(annotation1.getSuperclass());
    Assert.assertEquals(1, annotation1.getInterfaces().length);
    Assert.assertEquals(Annotation.class, annotation1.getInterfaces()[0]);
  }
  @Test
  public void test2() {
    Class<Annotation2> annotation2 = Annotation2.class;
    Assert.assertTrue(annotation2.isAnnotation());
    Field[] declaredFields = annotation2.getDeclaredFields();
    Assert.assertEquals(0, declaredFields.length);
    Method[] declaredMethods = annotation2.getDeclaredMethods();
    Assert.assertEquals(1, declaredMethods.length);
    Method value = declaredMethods[0];
    Assert.assertEquals("value", value.getName());
    Assert.assertEquals(0, value.getParameterTypes().length);
    Assert.assertEquals(Integer.TYPE, value.getReturnType());


    Assert.assertNull(annotation2.getSuperclass());
    Assert.assertEquals(1, annotation2.getInterfaces().length);
    Assert.assertEquals(Annotation.class, annotation2.getInterfaces()[0]);
  }

  @Test
  public void test3() {
    Class<Annotation2> annotation1 = Annotation2.class;

    Method[] declaredMethods = annotation1.getDeclaredMethods();
    Method value = declaredMethods[0];
    Assert.assertEquals(Integer.valueOf(6), value.getDefaultValue());
  }
  @Test
  public void test4() {
    Class<Annotation3> annotation3 = Annotation3.class;
    Assert.assertTrue(annotation3.isAnnotation());
    Field[] declaredFields = annotation3.getDeclaredFields();
    Assert.assertEquals(0, declaredFields.length);
    Method[] declaredMethods = annotation3.getDeclaredMethods();
    Assert.assertEquals(2, declaredMethods.length);
    for (Method method : declaredMethods) {
      if ("annotationValue1".equals(method.getName())) {
        Assert.assertEquals(Annotation1.class, method.getReturnType());
        Assert.assertNull(method.getDefaultValue());
      } else {
        Assert.assertEquals(Annotation2.class, method.getReturnType());
        Assert.assertEquals("annotationValue2", method.getName());
      }
    }

    Assert.assertNull(annotation3.getSuperclass());
    Assert.assertEquals(1, annotation3.getInterfaces().length);
    Assert.assertEquals(Annotation.class, annotation3.getInterfaces()[0]);
  }

  @Test
  public void test5() {
    Class<Annotation3> annotation3 = Annotation3.class;
    Method[] declaredMethods = annotation3.getDeclaredMethods();
    for (Method method : declaredMethods) {
      if ("annotationValue1".equals(method.getName())) {
        // checked in test4
      } else {
        Assert.assertTrue(method.getDefaultValue() instanceof Annotation2);
      }
    }
  }

  @Test
  public void test6() {
    printAllAnnotations(Annotation1.class);
    printAllAnnotations(Annotation2.class);
    printAllAnnotations(Annotation3.class);
    printAllAnnotations(Annotation4.class);
    printAllAnnotations(Annotation5.class);
    printAllAnnotations(Annotation6.class);
    printAllAnnotations(Annotation7.class);
    printAllAnnotations(Annotation8.class);
    printAllAnnotations(Annotation9.class);
    printAllAnnotations(Annotation10.class);
    printAllAnnotations(Annotation11.class);
    printAllAnnotations(Annotation12.class);
    printAllAnnotations(Annotation13.class);
    printAllAnnotations(Annotation14.class);
    printAllAnnotations(Annotated2.class);
    printAllAnnotations(Annotated3.class);
    printAllAnnotations(Annotated1.class);
    printAllAnnotations(Annotated4.class);

  }

  @Test
  public void test7() {
    Class<Annotated4> annotated = Annotated4.class;
    Annotation11 annotation = annotated.getAnnotation(Annotation11.class);
    Assert.assertEquals(void.class, annotation.value());
  }

  @Test
  public void test8() {
    Class<Annotated5> annotated = Annotated5.class;
    Annotation12 annotation = annotated.getAnnotation(Annotation12.class);
    Assert.assertTrue(annotation.value());
  }

  @Test
  public void test9() {
    printAnnotation(Annotation1.class);
    printAnnotation(Annotation2.class);
    printAnnotation(Annotation3.class);
    printAnnotation(Annotation4.class);
    printAnnotation(Annotation5.class);
    printAnnotation(Annotation6.class);
    printAnnotation(Annotation7.class);
    printAnnotation(Annotation8.class);
    printAnnotation(Annotation9.class);
    printAnnotation(Annotation10.class);
    printAnnotation(Annotation11.class);
    printAnnotation(Annotation12.class);
    printAnnotation(Annotation13.class);
    printAnnotation(Annotation14.class);
  }

  private static void printAllAnnotations(Class<?> clazz) {
    printAnnotations(clazz.getAnnotations());
    System.out.println("class " + clazz.getName() + " {");
    System.out.println();

    for (Field field : clazz.getDeclaredFields()) {
      printAnnotations(field.getAnnotations());
      System.out.println("  " + field.getType() + " " + field.getName() + ";");
      System.out.println();
    }

    for (Method method : clazz.getDeclaredMethods()) {
      printAnnotations(method.getAnnotations());
      System.out.print("  " + method.getReturnType() + " " + method.getName() + "(");
      Annotation[][] parameterAnnotations =  method.getParameterAnnotations();
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        printAnnotations(parameterAnnotations[i]);
        System.out.print(parameterTypes[i] + " arg" + i);
        if (i < parameterTypes.length - 1) {
          System.out.print(", ");
        }
      }
      System.out.print(")");
      if (method.getDefaultValue() != null) {
        System.out.print(" = " + method.getDefaultValue());
      }
      System.out.println();
      System.out.println();
    }
    System.out.println("}");
  }

  private static void printAnnotation(Class<?> annotationClass) {
    System.out.println("@interface " + annotationClass.getName() + " {");

    for (Method method : annotationClass.getDeclaredMethods()) {
      System.out.print("  " + method.getReturnType() + " " + method.getName());
      Object defaultValue = method.getDefaultValue();
      if (defaultValue != null) {
        System.out.print(" default " + defaultValue);
      }
      System.out.println(";");
    }
    System.out.println("}");

  }

  private static void printAnnotations(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      printAnnotation(annotation);
    }
  }
  /**
   * @param annotation
   */
  private static void printAnnotation(Annotation annotation) {
    System.out.println(annotation);
  }
}
