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

package com.android.jack.shrob.test011.jack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class A {
  public static Object keep1() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
  SecurityException, InstantiationException {
    Class<?> bClass = Class.forName("com.android.jack.shrob.test011.jack.B");
    Object o = bClass.newInstance();

    try {
      String cName = "com.android.jack.shrob.test011.jack.C";
      Class<?> cClass = Class.forName(cName);
      throw new RuntimeException();
    } catch (ClassNotFoundException e) {
      // expected (used to verify the code has been shrunk)
    }
    return o;
  }

  public static int keep2() throws IllegalAccessException, IllegalArgumentException,
  InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    Object o = B.class.newInstance();
    Method m = B.class.getMethod("value");
    return ((Integer) m.invoke(o, (Object[]) null)).intValue();
  }
}
