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

package com.android.jack.java8.lambda.test020.jack;

interface I {
  int m(int i);
}

interface OutOfMethod {
  int inc(int i);
}

public class Lambda {

  public int test1(int i) {

    class InMethod {
      int inc(int i) {
        I lambda = c -> c + 1;
        return lambda.m(i);
      }
    }

    InMethod c = new InMethod();

    return c.inc(i);
  }

  public int test2(int i) {

    OutOfMethod anonymous = new OutOfMethod () {

      @Override
      public int inc(int i) {
        I lambda = c -> c + 2;
        return lambda.m(i);
      }

    };

    return anonymous.inc(i);
  }

  public int test3(int i) {

    I anonymous = new I () {

      @Override
      public int m(int i) {
        I lambda = c -> c + 3;
        return lambda.m(i);
      }

    };

    return anonymous.m(i);
  }

}
