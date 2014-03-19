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

package com.android.jack.trycatch.test002.jack;

import java.util.LinkedList;
import java.util.List;


public class TryCatch {

  public static Object tryWithLoop1() {
    try {
      for(int i = 0; i<1; i++) {
        i++;
      }

    } catch (RuntimeException e) {
      return new Object();
    }
    return null;
  }

  public static Object tryWithLoop2() {
    try {
      for(int i = 0; i<1; i++) {
        m(i);
      }

    } catch (RuntimeException e) {
      return null;
    }
    return new Object();
  }


  public static Object tryWithLoop3() {
    try {
      for(int i = 0; i<1; i++) {
        i++;
      }

    } catch (RuntimeException e) {
    }
    return null;
  }

  public static Object tryWithLoop4() {
    try {
      for(int i = 0; i<1; i++) {
        m(i);
      }

    } catch (RuntimeException e) {
    }
    return null;
  }

  private static void m(int i) {
    throw new NullPointerException();
  }

  public static int tryWithLoop5() {
    List<Integer> values = new LinkedList<Integer>();
    values.add(new Integer(1));
    values.add(new Integer(2));
    values.add(new Integer(3));
    try {
      try {
        return 1;
      } catch (Throwable ex) {
        return 2;
      }
    } finally {
      for (Integer i : values)
        i.intValue();
    }
  }

}
