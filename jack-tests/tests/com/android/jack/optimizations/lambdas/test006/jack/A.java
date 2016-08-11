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

package com.android.jack.optimizations.lambdas.test006.jack;

interface Producer {
  String run();
}

public class A {
  private static String ID = "A;";

  static String run(Producer r) {
    return r.run();
  }

  public static String test() {
    return A.run(() -> ID)
        + AA.test()
        + AB.test()
        + B.test();
  }

  private static class AA {
    private static String ID = "AA;";

    static String test() {
      return A.run(() -> ID) + AAA.test();
    }

    private static class AAA {
      private static String ID = "AAA;";

      static String test() {
        return A.run(() -> ID);
      }
    }
  }

  private static class AB {
    private static String ID = "AB;";

    static String test() {
      return A.run(() -> ID);
    }
  }
}

class B {
  private static String ID = "B;";

  public static String test() {
    return A.run(() -> ID)
        + BA.test()
        + BB.test();
  }

  private static class BA {
    private static String ID = "BA;";

    static String test() {
      return A.run(() -> ID);
    }
  }

  private static class BB {
    private static String ID = "BB;";

    static String test() {
      return A.run(() -> ID);
    }
  }
}
