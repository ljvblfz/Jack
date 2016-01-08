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

package com.android.jack.java8.annotation.test002.jack;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Authors.class)
@Retention(RetentionPolicy.SOURCE)
@interface Author {
    String name();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Single {
  String name();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Authors {
    Author[] value();
}

@Author(name = "A")
@Single(name = "0")
@Author(name = "B")
public class Annotation002 {

  @Author(name = "C")
  @Single(name = "1")
  @Author(name = "D")
  public static int field;

  @Author(name = "E")
  @Single(name = "2")
  @Author(name = "F")
  public void method() {
  }

  public void methodWithParameter(@Author(name = "G") @Author(name = "H") int i) {
  }

  public Author[] getAuthorsAnnotationFromClass() {
    return  Annotation002.class.getAnnotation(Authors.class).value();
  }

  public Author[] getAuthorsAnnotationFromField() throws NoSuchFieldException {
    return Annotation002.class.getField("field").getAnnotation(Authors.class).value();
  }

  public Author[] getAuthorsAnnotationFromMethod() throws NoSuchMethodException {
    return Annotation002.class.getMethod("method").getAnnotation(Authors.class).value();
  }

  public Author[] getAuthorsAnnotationFromMethodParameter() throws NoSuchMethodException {
    return ((Authors) Annotation002.class.getMethod("methodWithParameter", int.class)
        .getParameterAnnotations()[0][0]).value();
  }
}
