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

package com.android.jack.bridge.test009.dx;

import com.android.jack.bridge.test009.jack.sub.PublicExtendsPackage;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Tests {

  @Test
  public void testBridgeFlags() throws Exception {
    Class<?> clazz = PublicExtendsPackage.class;
    Method[] bridges = new Method[]{clazz.getDeclaredMethod("put", Boolean.TYPE),
                                    clazz.getDeclaredMethod("put", Float.TYPE),
                                    clazz.getDeclaredMethod("put", Long.TYPE),
                                    clazz.getDeclaredMethod("put", Object[].class)};

    for (Method method : bridges) {
      checkBridgeFlags(method);
    }
  }

  private void checkBridgeFlags(Method bridge) {
    String message = bridge.toString();
    Assert.assertTrue(message, bridge.isBridge());
    Assert.assertFalse(message, (bridge.getModifiers() & Modifier.NATIVE) != 0);
    Assert.assertFalse(message, (bridge.getModifiers() & Modifier.ABSTRACT) != 0);
    Assert.assertFalse(message, (bridge.getModifiers() & Modifier.SYNCHRONIZED) != 0);
    Assert.assertFalse(message, (bridge.getModifiers() & Modifier.STRICT) != 0);
  }
}
