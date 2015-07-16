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

package com.android.jack.switchstatement.test027.jack;

public class Switch1 {
  private static class InnerClass1 {
    private enum Enum1 {
      VALUE1, VALUE2, VALUE3, VALUE4, VALUE5
    }

    public static int switch1(Enum1 enum1) {
      switch (enum1) {
        case VALUE1:
          return 1;
        case VALUE2:
          return 2;
        case VALUE3:
          return 3;
        case VALUE4:
          return 4;
        case VALUE5:
          return 5;
        default:
          return 0;
      }
    }

    private static class InnerClass2 {
      private enum Enum2 {
        VALUE1, VALUE2, VALUE3, VALUE4, VALUE5
      }

      public static int switch2(Enum2 enum2) {
        switch (enum2) {
          case VALUE1:
            return -1;
          case VALUE2:
            return -2;
          case VALUE3:
            return -3;
          case VALUE4:
            return -4;
          case VALUE5:
            return -5;
          default:
            return 0;
        }
      }
    }
  }

  public static boolean switch1(Object o) {
    InnerClass1.Enum1 e = (InnerClass1.Enum1) o;
    return e.ordinal() + 1 == InnerClass1.switch1(e);
  }

  public static boolean switch2(Object o) {
    InnerClass1.InnerClass2.Enum2 e = (InnerClass1.InnerClass2.Enum2) o;
    return e.ordinal() + 1 == -InnerClass1.InnerClass2.switch2(e);
  }
}
