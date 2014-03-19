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

package com.android.jack.switchstatement.test008.jack;

public class Switch {

  public final class SwitchValues {
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int UNSUPPORTED = 255;
  }

  /**
   * Check that the control flow graph does not contains useless edge/block when a case fall-through
   * to the default case. The case SwitchValues.UNSUPPORTED and SwitchValues.ZERO are uselesses and
   * must be remove to generate packed switch instead of sparsed switch.
   */
  public static int switch001(int switchValue) {
    int retValue;
    switch (switchValue) {
      case SwitchValues.ONE: {
        retValue = 1;
        break;
      }
      case SwitchValues.ZERO:
      case SwitchValues.UNSUPPORTED: {
      }
      default:
        retValue = -1;
    }

    return retValue;
  }
}
