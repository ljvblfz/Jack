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

package com.android.jack.trycatch.test001.jack;

public class TryCatch {

 public static int div(int i) {
   try {
     int r = 1 / i;
     return -1;
   } catch (Throwable t) {
     return 0;
   }
 }

 public static int nestedDiv(int i, int j) {
   try {
     int r1 = 1 / i;
     return -1;
   } catch (Throwable t1) {
     try {
       int r2 = 1 / j;
       return -2;
     } catch (Throwable t2) {
       return 0;
     }
   }
 }

  public static int emptyCatch(int i) {
    try {
      int r = 1 / i;
      return -1;
    } catch (Throwable t) {
    }
    return 0;
  }

  public static int nestedTry(int i) {
    try {
      try {
        int r = 1 / i;
        return -1;
      } catch (Throwable t) {
      }
    } catch (Throwable t) {
      return 0;
    }
    return 1;
  }

  public static int nestedTry2(int i) {
    int result = 1;
    try {
      try {
        int r = 1 / i;
        return -1;
      } catch (Throwable t) {
        result = 5;
      }
    } catch (Throwable t) {
      return result;
    }
    return result;
  }

  public static int nestedTryWithFallThrough(int i) {
    int result = 0;
    try {
      try {
        result = result + 2;
      } catch (Throwable t) {
        result = 0;
      }
      result = result + 2;
      int r = 1 / i;
    } catch (Throwable t) {
      result = result + 2;
    }
    result = result + 2;
    return result;
  }

  public static int similarCatches() {
    int result = 0;
    try {
      try {
        throw new AssertionError();
      } catch (AssertionError t) {
        result += 1;
      }
    } catch (AssertionError t) {
      result += 2;
    }
    return result;
  }
}
