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

package com.android.jack.annotation.bridge.test007.dx;

import com.android.jack.annotation.bridge.test007.jack.Anno;
import com.android.jack.annotation.bridge.test007.jack.sub.PublicExtendsGeneric;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Tests {

  @Test
  public void test1() throws NoSuchMethodException {
    checkAnnotatedBridge(PublicExtendsGeneric.class, "put", Void.TYPE, Object.class);
  }

  private static void checkAnnotatedBridge(Class<?> declaringClass, String name, Class<?> returnType,
      Class<?>... parameterType) throws NoSuchMethodException {
    Method method = getDeclaredMethod(declaringClass, name, returnType, parameterType);
    Assert.assertTrue(method.isBridge());
    Assert.assertNotNull(method.getAnnotation(Anno.class));
    forAnnotations:
    for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
      for (Annotation annotation : paramAnnotations) {
        if (Anno.class.isInstance(annotation)) {
          continue forAnnotations;
        }
      }
      Assert.fail(method.toString());
    }
  }

  private static Method getDeclaredMethod(Class<?> declaringClass, String name, Class<?> returnType,
      Class<?>... parameterType) throws NoSuchMethodException {
    Method[] methods = declaringClass.getMethods();
    for (Method method : methods) {
      if (method.getName().equals(name)
          && method.getReturnType().equals(returnType)
          && Arrays.equals(method.getParameterTypes(), parameterType)) {
        return method;
      }
    }
    throw new NoSuchMethodException();
  }
}
