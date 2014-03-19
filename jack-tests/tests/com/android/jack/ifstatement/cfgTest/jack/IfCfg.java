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

package com.android.jack.ifstatement.cfgTest.jack;

/**
 * Tests about if statement and cfg builder.
 */
public class IfCfg {

  public static int ifWithEndedThen(boolean val) {
    if (val) {
      return 1;
    }
    return 2;
  }

  public static int ifWithEndedThenElse(boolean val) {
    if (val) {
      return 1;
    } else {
      return 2;
    }
  }

  public static int ifWithThen(boolean val) {
    int result = 0;
    if (val) {
      result = result + 2;
    }
    return result;
  }

  public static int ifWithThenElse(boolean val) {
    int result = 0;
    if (val) {
      result = result + 2;
    } else {
      result = result + 3;
    }
    return result;
  }

  public static int ifThenNested(int val) {
    int result = 0;
    if (val == 1) {
      result = result + 2;
      if (val != 1) {
        result = result + 2;
      }
    }
    result = result + 2;
    return result;
  }

  public static int ifElseNested(int val) {
    int result = 0;
    if (val == 1) {
      result = result + 2;
    } else {
      if (val == 2) {
        result = result + 2;
      }
    }
    result = result + 2;
    return result;
  }

  public static int ifEmptyThen(int val) {
    int result = 2;
    if (val == 1) {
    }
    return result;
  }

  public static int ifEmptyElse(int val) {
    int result = 2;
    if (val == 1) {
      result = 4;
    } else {
    }
    return result;
  }

  public static int ifThenElseNested(int val) {
    int result = 0;
    if (val == 1) {
      result = 3;
      if (val == 2) {
        result = 2;
      }
      result = 1;
    } else {
      if (val == 2) {
        result = 2;
      } else {
        result = 3;
      }
    }
    return result;
  }
}
