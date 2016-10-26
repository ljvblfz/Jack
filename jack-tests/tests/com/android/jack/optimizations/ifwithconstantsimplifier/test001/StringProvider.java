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

package com.android.jack.optimizations.ifwithconstantsimplifier.test001;

public class StringProvider {
  private final String string1;
  private final String string2;

  public StringProvider(String string1, String string2) {
    this.string1 = string1;
    this.string2 = string2;
  }

  public String getString1() {
    return string1;
  }

  public String getString2() {
    return string2;
  }
}