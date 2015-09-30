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
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.defaultmethods.test026.jack;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class Tests {

  @Test
  public void default_methods_of_library_interfaces_are_ignored_silently() throws Exception {
      @SuppressWarnings("unchecked") Iterator<String> dummy = new Iterator<String>() {

        @Override
        public boolean hasNext() {
          throw new AssertionError();
        }

        @Override
        public String next() {
          throw new AssertionError();
        }

        @Override
        public void remove() {
          throw new AssertionError();
        }

      };

      Iterable<String> it = new Iterable<String>() {
          @Override
          public Iterator<String> iterator() {
              return dummy;
          }
      };

      Assert.assertEquals("interface should work as usual", it.iterator(), dummy);
      Assert.assertEquals("should not copy default methods from library interfaces",
              it.getClass().getDeclaredMethods().length, 1);
  }

}

