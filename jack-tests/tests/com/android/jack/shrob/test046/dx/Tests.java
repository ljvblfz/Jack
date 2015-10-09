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

package com.android.jack.shrob.test046.dx;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Tests {

  @Test
  public void test() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
                            IllegalArgumentException, InvocationTargetException,
                            InstantiationException {
    Class<?> c = Class.forName("renamedCom.renamedAndroid.renamedJack.renamedShrob."
        + "renamedTest046.renamedJack.renamedC");
    Method a = c.getMethod("a", Object.class);
    int result = ((Integer) a.invoke(c.newInstance(), new Object[] {null})).intValue();
    Assert.assertEquals(2, result);
  }

}
