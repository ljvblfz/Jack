/*
 * Copyright (C) 2015 The Android Open Source Project
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
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.defaultmethods.test027.jack;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Tests {

  @SomeAnnotation(1)
  private interface AnnotatedInterface {

      @SomeAnnotation(2)
      void annotatedAbstractMethod();

      @SomeAnnotation(3)
      default void annotatedDefaultMethod() {
      }

      @SomeAnnotation(4)
      static void annotatedStaticMethod() {
      }
  }

  @Retention(value = RetentionPolicy.RUNTIME)
  private @interface SomeAnnotation {
      int value();
  }

  private static boolean checkAnnotationValue(Annotation[] annotations, int value) {
    Assert.assertEquals(annotations.length, 1);
    return annotations[0] instanceof SomeAnnotation && ((SomeAnnotation) annotations[0]).value() == value;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void keeps_annotations_on_interface_methods() throws Exception {
      Assert.assertTrue("interface", checkAnnotationValue(AnnotatedInterface.class.getAnnotations(), 1));

      Assert.assertTrue("abstract method", checkAnnotationValue(AnnotatedInterface.class.getMethod("annotatedAbstractMethod").getAnnotations(), 2));

      Assert.assertTrue("default method", checkAnnotationValue(AnnotatedInterface.class.getMethod("annotatedDefaultMethod").getAnnotations(), 3));

      Assert.assertTrue("static method", checkAnnotationValue(AnnotatedInterface.class.getMethod("annotatedStaticMethod").getAnnotations(), 4));
  }

}

