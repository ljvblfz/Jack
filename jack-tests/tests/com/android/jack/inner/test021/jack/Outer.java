/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.inner.test021.jack;

public class Outer {

  static class OuterRefFromSuperCtorBase {
    OuterRefFromSuperCtorBase(Object o) {
      o.toString();
    }
  }

  class OuterRefFromSuperCtorCall extends OuterRefFromSuperCtorBase {
    OuterRefFromSuperCtorCall() {
      super(new Object() {
        @Override
        public String toString() {
          value = 1;
          return "";
        }
      });
    }
  }

  class OuterRefFromThisCtorCall extends OuterRefFromSuperCtorBase {
    public OuterRefFromThisCtorCall(Object object) {
      super(object);
    }

    public OuterRefFromThisCtorCall() {
      this(new Object() {
        @Override
        public String toString() {
          value = 2;
          return "";
        }
      });
    }
  }

  private int value = 0;

  public int testOuterThisFromSuperCall() {
    new OuterRefFromSuperCtorCall();
    return value;
  }

  public int testOuterThisFromThisCall() {
    new OuterRefFromThisCtorCall();
    return value;
  }
}
