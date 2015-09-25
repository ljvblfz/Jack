/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.defaultmethod.test012.jack;

interface ISuper {
  default int count(int a, int b) {
      if (a == 0) {
          return b;
      }
      return this.count(a - 1, b + b);
  }
}

interface I extends ISuper {
  @Override
  default int count(int a, int b) {
      return ISuper.super.count(a,b);
  }
}

/**
 * Check that default method can invoke super default method.
 */
public class DefaultMethod implements I {

  public int test(int v1, int v2) {
    return count(v1, v2);
  }
}
