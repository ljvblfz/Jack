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

package com.android.jack.java8.defaultmethod.test013.jack;

interface ISuper1 {
  default int getvalue() {
    return 1;
  }
}

interface ISuper2 {
  default int getvalue() {
    return 2;
  }
}

/**
 * Disambiguous default method.
 */
public class DefaultMethod implements ISuper1, ISuper2 {

  @Override
  public int getvalue() {
    return ISuper2.super.getvalue();
  }

  public int test() {
    return getvalue();
  }
}
