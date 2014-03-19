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

package com.android.jack.switchstatement.test005.jack;

public class Switch {
  public static int switch001(int a) {
    switch (a) {
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        return 3;
    }
  }

  public static int switch002(int a) {
    switch (a) {
      case 1:
        return 1;
      case 2: {
        switch (a) {
          case 1:
            return 11;
          default:
            break;
        }
        return 2;
      }
    }
    return 3;
  }

  public static int switch003(int a) {
    switch (a) {
      case (byte) 1:
        return 1;
      case (short) 3:
        return 3;
      case (char) 2:
        return 2;
      default:
        return 3;
    }
  }

  public static int switch004(int a) {
    switch (a) {
      case 1:
        return 1;
      case 3:
        return 3;
      case (char) 2:
        return 2;
      case (char) -1:
        return -1;
      case  -2:
        return -2;
      default:
        return 3;
    }
  }
}
