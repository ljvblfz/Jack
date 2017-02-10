/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.string.concat004.jack;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  public String field = "a" + "b";

  @Test
  public void test1() {
    String str = "a" + "b";
    Assert.assertTrue(str == "ab");
  }

  @Test
  public void test2() {
    String str = "ab";
    Assert.assertTrue(str == field);
  }


  @Test
  public void test3() {
    String str = "a" + null;
    Assert.assertFalse(str == "anull");
  }

  @Test
  public void test4() {
    String str = (String) null + null;
    Assert.assertFalse(str == "nullnull");
  }
}
