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

package com.android.jack.annotation.test019.jack;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Tests {

  @Test
  public void test1() {
    Annotation[] classAnnotations = Annotated.class.getAnnotations();
    checkAnnotations(Annotated.class.getAnnotations(), "Annotated");

    for (Method method : Annotated.class.getDeclaredMethods()) {
      checkAnnotations(method.getAnnotations(), method.getName());
    }
    for (Constructor<?> constructor : Annotated.class.getConstructors()) {
      checkAnnotations(constructor.getAnnotations(), "<init>");
    }
  }

  private void checkAnnotations(Annotation[] classAnnotations, String name) {
    Assert.assertEquals(name, 1, classAnnotations.length);
    Assert.assertEquals(name, ((Check) classAnnotations[0]).value());
  }

}
