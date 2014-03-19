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

package com.android.jack.conditional.test006.jack;

public class ConditionalTest006 {

  // Check that null is assigned to f1
  public static Float test001() {
    @SuppressWarnings({"boxing", "unused"})
    Float f1 = false? 1.0f: null;
    return f1;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Float test002() {
    @SuppressWarnings({"boxing", "unused"})
    Float f2 = false? 1.0f: false? 1.0f: null;
    return f2;
  }
}
