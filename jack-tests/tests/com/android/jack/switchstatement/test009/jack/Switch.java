/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.switchstatement.test009.jack;

/**
 * Test allowing to check that 'packed-switch-payload' into generated dex is as small as possible.
 */
public class Switch {

  public static enum Num {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN
  }

  public static boolean switch1(Num num) {
    switch (num) {
      case ZERO:
      case ONE:
      case TWO:
      case THREE:
      case TEN:
        return true;
      default:
        return false;
    }
  }
}
