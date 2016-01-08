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

package com.android.jack.java8.annotation.test001.jack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Annotation tests.
 */
public class Tests {

  @Test
  public void testGetAnnotationFromClass() {
    Author[] authors = new Annotation001().getAuthorsAnnotationFromClass();
    Assert.assertTrue((authors[0].name().equals("A") && authors[1].name().equals("B"))
        || (authors[0].name().equals("B") && authors[1].name().equals("A")));
  }

  @Test
  public void testGetAnnotationFromField() {
    try {
      Author[] authors = new Annotation001().getAuthorsAnnotationFromField();
      Assert.assertTrue((authors[0].name().equals("D") && authors[1].name().equals("C"))
          || (authors[0].name().equals("C") && authors[1].name().equals("D")));
    } catch (NoSuchFieldException e) {
      Assert.fail();
    }
  }

  @Test
  public void testGetAnnotationFromMethod() {
    try {
      Author[] authors = new Annotation001().getAuthorsAnnotationFromMethod();
      Assert.assertTrue((authors[0].name().equals("E") && authors[1].name().equals("F"))
          || (authors[0].name().equals("E") && authors[1].name().equals("F")));
    } catch (NoSuchMethodException e) {
      Assert.fail();
    }
  }

  @Test
  public void testGetAnnotationFromMethodParameter() {
    try {
      Author[] authors = new Annotation001().getAuthorsAnnotationFromMethodParameter();
      Assert.assertTrue((authors[0].name().equals("G") && authors[1].name().equals("H"))
          || (authors[0].name().equals("H") && authors[1].name().equals("G")));
    } catch (NoSuchMethodException e) {
      Assert.fail();
    }
  }
}
