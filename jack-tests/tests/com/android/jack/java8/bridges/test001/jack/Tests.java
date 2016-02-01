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

package com.android.jack.java8.bridges.test001.jack;

import java.lang.reflect.Method;

import org.junit.Test;

interface GenericInterface<T> {
  T m(T t);
}


interface ParameterizedInterface extends GenericInterface<String> {
  @Override
  String m(String t);
}


public class Tests {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void test001() {
    GenericInterface gi = new ParameterizedInterface() {
      @Override
      public String m(String t) {
        return "Anonynous";
      }
    };

    try {
      gi.m(new Object());
    } catch (ClassCastException e) {
      // Ok
    }

    for (Method m : GenericInterface.class.getMethods()) {
      if (m.isBridge()) {
        throw new AssertionError();
      }
    }


    for (Method m : ParameterizedInterface.class.getMethods()) {
      if (m.isBridge()) {
        throw new AssertionError();
      }
    }
  }

}
