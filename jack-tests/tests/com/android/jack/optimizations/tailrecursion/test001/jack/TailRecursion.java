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

package com.android.jack.optimizations.tailrecursion.test001.jack;

public class TailRecursion {

  public static int test001(int k, int ret) {
    if (k > 0) {
      return test001(k-1, ret * k);
    }
    return ret;
  }

  public static class RecursionTracker {
    private String methodName;
    private boolean recursionDetected = false;

    RecursionTracker(String methodName) {
      this.methodName = methodName;
    }

    public boolean recursionDetected() {
      return recursionDetected;
    }

    public void mark() {
      if (!recursionDetected) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        boolean foundAlready = false;
        for (int i = 0; i < stack.length; i++) {
          if (stack[i].toString().contains(methodName)) {
            if (foundAlready) {
              recursionDetected = true;
              return;
            }
            foundAlready = true;
          }
        }
      }
    }
  }

  public static final RecursionTracker staticFooRecTracker = new RecursionTracker("Clazz.staticFoo");
  public static final RecursionTracker finalFooRecTracker = new RecursionTracker("Clazz.finalFoo");
  public static final RecursionTracker finalParamFooRecTracker = new RecursionTracker("finalParamFoo");
  public static final RecursionTracker privateFooRecTracker = new RecursionTracker("Clazz.privateFoo");
  public static final RecursionTracker instanceFooRecTracker = new RecursionTracker("Clazz.instanceFoo");

  static class Clazz {
    static int staticFoo(int k, int ret) {
      staticFooRecTracker.mark();
      if (k > 1) {
        return staticFoo(k - 1, ret * k);
      }
      return ret;
    }

    private int privateFoo(int k, int ret) {
      privateFooRecTracker.mark();
      if (k > 1) {
        return privateFoo(k - 1, ret * k);
      }
      return ret;
    }

    int instanceFoo(int k, int ret) {
      instanceFooRecTracker.mark();
      if (k > 1) {
        return instanceFoo(k - 1, ret * k);
      }
      return ret;
    }

    final int finalFoo(int k, int ret) {
      finalFooRecTracker.mark();
      if (k > 1) {
        return finalFoo(k - 1, ret * k);
      }
      return ret;
    }

    final int finalParamFoo(final int finalK, int ret) {
      finalParamFooRecTracker.mark();
      if (finalK > 1) {
        return finalParamFoo(finalK - 1, ret * finalK);
      }
      return ret;
    }
  }

  static class DerivedClazz extends Clazz {
    @Override int instanceFoo(int k, int ret) {
      return super.instanceFoo(k, ret) * 2;
    }
  }

  public static int test002(int k) {
    return new Clazz().instanceFoo(k, 1);
  }

  public static int test003(int k) {
    return new DerivedClazz().instanceFoo(k, 1);
  }

  public static int test004(int k) {
    return Clazz.staticFoo(k, 1);
  }

  public static int test005(int k) {
    return new Clazz().privateFoo(k, 1);
  }

  public static int test006(int k) {
    return new Clazz().finalFoo(k, 1);
  }

  public static int test007(int k) {
    return new Clazz().finalParamFoo(k, 1);
  }
}
