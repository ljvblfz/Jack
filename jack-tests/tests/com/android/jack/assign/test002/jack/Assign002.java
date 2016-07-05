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

package com.android.jack.assign.test002.jack;

import junit.framework.TestCase;

/**
 * Check that Jack does not generate the error 'The local variable i may not have been initialized'.
 */
public class Assign002 extends TestCase {

  public static void test() {
    int i = 0;
    assertTrue((i = 2) == 2);
    assertTrue(i == 2);
  }
}
