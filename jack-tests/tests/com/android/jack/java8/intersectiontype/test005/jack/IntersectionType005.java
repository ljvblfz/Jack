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

package com.android.jack.java8.intersectiontype.test005.jack;

interface I1 {
  int getValue2();
  int getValue3();
}

interface I {
  int getValue();
}

/**
 * Check that this test raises a compilation error due to A into the intersection type.
 */
public class IntersectionType005 {

  public int test() {
    return ((I & I1) () -> {
      return 33;
    }).getValue();
  }
}
