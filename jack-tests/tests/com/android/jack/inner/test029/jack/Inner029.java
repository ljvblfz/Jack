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

package com.android.jack.inner.test029.jack;


class Inner029SuperSuper {
  public int getValue() {
    return 10;
  }
}

class Inner029Super extends Inner029SuperSuper {
}

class InnerSuper extends Inner029Super {
  @Override
  public int getValue() {
    return 20;
  }
}

public class Inner029 extends Inner029Super {

  public int result = new Inner().getInner2().test();

  class Inner extends InnerSuper {

    Inner2 getInner2() {
      return new Inner2();
    }

    class Inner2 {
      public int test() {
        return Inner029.super.getValue();
      }
    }
  }
}
