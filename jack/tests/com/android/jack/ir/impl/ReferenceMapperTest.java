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

package com.android.jack.ir.impl;

import junit.framework.Assert;

import org.junit.Test;

/**
 * JUnit test for ReferenceMapper.
 */
public class ReferenceMapperTest {

  private static final String[] TYPES =
    { "Z", "B", "C", "S", "I", "J", "F", "D", "Ljava/lang/Object;" };

  private void testForEachReturnType(int expectedResult, String signature) {
    Assert.assertEquals(expectedResult, ReferenceMapper.countParams("(" + signature + ")"));
    Assert.assertEquals(expectedResult, ReferenceMapper.countParams("(" + signature + ")V"));
    for (String returnType : TYPES) {
      Assert.assertEquals(expectedResult,
          ReferenceMapper.countParams("(" + signature + ")" + returnType));
    }
  }

  /**
   * Verifies that countParams works for signatures with no params.
   */
  @Test
  public void testNoParams() throws Exception {
    testForEachReturnType(0, "");
  }

  /**
   * Verifies that countParams works for signatures with one param.
   */
  @Test
  public void testSingleParam() throws Exception {
    for (String type : TYPES) {
      testForEachReturnType(1, type);
    }
  }

  /**
   * Verifies that countParams works for signatures with two params.
   */
  @Test
  public void testTwoParams() throws Exception {
    for (String type1 : TYPES) {
      for (String type2 : TYPES) {
        testForEachReturnType(2, type1 + type2);
      }
    }
  }

  /**
   * Verifies that countParams works for signatures with three params.
   */
  @Test
  public void testThreeParams() throws Exception {
    for (String type1 : TYPES) {
      for (String type2 : TYPES) {
        for (String type3 : TYPES) {
          testForEachReturnType(3, type1 + type2 + type3);
        }
      }
    }
  }

  /**
   * Verifies that countParams works for signatures with array param.
   */
  @Test
  public void testArrayParam() throws Exception {
    for (String type : TYPES) {
      testForEachReturnType(1, "[" + type);
      testForEachReturnType(1, "[[" + type);
      testForEachReturnType(1, "[[[" + type);
      testForEachReturnType(1, "[[[[" + type);
      testForEachReturnType(1, "[[[[[" + type);
    }
    for (String type1 : TYPES) {
      for (String type2 : TYPES) {
        testForEachReturnType(2, "[" + type1 + type2);
        testForEachReturnType(2, type1 + "[" +type2);
        testForEachReturnType(2, "[" + type1 + "[" +type2);
        testForEachReturnType(2, "[[" + type1 + type2);
        testForEachReturnType(2, type1 + "[[" +type2);
        testForEachReturnType(2, "[[" + type1 + "[" +type2);
        testForEachReturnType(2, "[" + type1 + "[[" +type2);
        testForEachReturnType(2, "[[" + type1 + "[[" +type2);
      }
    }
    for (String type1 : TYPES) {
      for (String type2 : TYPES) {
        for (String type3 : TYPES) {
          testForEachReturnType(3, "[" + type1 + type2 + type3);
          testForEachReturnType(3, type1 + "[" + type2 + type3);
          testForEachReturnType(3, type1 + type2 + "[" + type3);
          testForEachReturnType(3, "[" + type1 + "[" + type2 + type3);
          testForEachReturnType(3, "[" + type1 + type2 + "[" + type3);
          testForEachReturnType(3, type1 + "[" + type2 + "[" + type3);
          testForEachReturnType(3, "[" + type1 + "[" + type2 + "[" + type3);
        }
      }
    }
  }

}
